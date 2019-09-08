package com.mengcraft.simpleorm.lib;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.val;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created on 17-6-26.
 */
@Data
@EqualsAndHashCode(exclude = {"file", "sublist", "clazz"})
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(exclude = {"file", "sublist", "clazz"})
public class MavenLibrary extends Library {

    private final String repository;
    private final String group;
    private final String artifact;
    private final String version;
    private final String clazz;

    private File file;
    private List<Library> sublist;

    @Override
    public File getFile() {
        if (file == null) {
            file = new File("lib", group.replace(".", File.separator)
                    + File.separator
                    + artifact
                    + File.separator
                    + artifact + '-' + version + ".jar");
        }
        return file;
    }

    @SneakyThrows
    @Override
    public List<Library> getSublist() {
        if (sublist == null) {
            val xml = new File(getFile().getParentFile(), getFile().getName() + ".pom");
            Node pom = XMLHelper.getDocument(xml).getFirstChild();
            while (!pom.getNodeName().equals("project")){
                pom=pom.getNextSibling();
            }
            val all = XMLHelper.getElement(pom, "dependencies");
            if (all == null) return (sublist = ImmutableList.of());
            val p = XMLHelper.getElement(pom, "properties");
            Builder<Library> b = ImmutableList.builder();

            val list = XMLHelper.getElementList(all, "dependency");
            String groupId=null;
            String artifactId=null;
            for (val depend : list) {

                val scope = XMLHelper.getElementValue(depend, "scope");
                //optional=true并不需要加载该依赖
                if (scope == null ) {
                val optional = XMLHelper.getElementValue(depend, "optional");
                    groupId = XMLHelper.getElementValue(depend, "groupId");
                    artifactId = XMLHelper.getElementValue(depend, "artifactId");
                    String version = XMLHelper.getElementValue(depend, "version");
                    System.out.println("依赖"+groupId+":"+artifactId+":"+version+",optional:"+optional);
                    if (optional!=null&&optional.equals("true")){
                        continue;
                    }
                    if (version == null){
                        version=this.version;
                        System.out.println("加载依赖库没有版本号，套用当前版本");
                    }
                    // TODO Request any placeholder support
                    if (version.startsWith("${")) {
                        val sub = version.substring(2, version.length() - 1);
                        version = XMLHelper.getElementValue(p, sub);
                    }
                    b.add(new MavenLibrary(repository,
                            groupId,
                            artifactId,
                            version,
                            null
                    ));
                }

            }
            sublist = b.build();
        }
        return sublist;
    }

    @SneakyThrows
    public void init() {
        if (!(getFile().getParentFile().isDirectory() || getFile().getParentFile().mkdirs())) {
            throw new IOException("mkdir");
        }

        loadFile(ImmutableSet.of(repository, Repository.CENTRAL.repository,Repository.ALIYUN.repository).iterator());
    }

    void loadFile(Iterator<String> repo) throws IOException {
        //http://101.110.118.29/central.maven.org/maven2/xerces/xercesImpl/2.6.2/xercesImpl-2.6.2.jar
        String artifact=this.artifact;
        if (artifact.equals("xerces-impl")){
            artifact="xercesImpl";
        }
        val url = repo.next()
                + '/'
                + group.replace('.', '/')
                + '/'
                + artifact
                + '/'
                + version
                + '/'
                + artifact + '-' + version;
        try {
            Files.copy(new URL(url + ".jar").openStream(),
                    getFile().toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            Files.copy(new URL(url + ".jar.md5").openStream(),
                    new File(getFile().getParentFile(), getFile().getName() + ".md5").toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            Files.copy(new URL(url + ".pom").openStream(),
                    new File(getFile().getParentFile(), getFile().getName() + ".pom").toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException io) {
            if (!repo.hasNext()) {
                throw new IOException("NO MORE REPOSITORY TO TRY", io);
            }
            loadFile(repo);
        }
    }

    @SneakyThrows
    public boolean isLoadable() {
        if (getFile().isFile()) {
            val check = new File(file.getParentFile(), file.getName() + ".md5");
            if (check.isFile()) {
                val buf = ByteBuffer.allocate(1 << 16);
                FileChannel channel = FileChannel.open(file.toPath());
                while (!(channel.read(buf) == -1)) {
                    buf.flip();
                    MD5.update(buf);
                    buf.compact();
                }
                return Files.newBufferedReader(check.toPath()).readLine().split(" ")[0].equals(MD5.digest());
            }
        }
        return false;
    }

    @Override
    public boolean present() {
        if (clazz == null || clazz.isEmpty()) return false;
        try {
            val result = Class.forName(clazz);
            return !(result == null);
        } catch (Exception ign) {
        }
        return false;
    }

    public enum Repository {

        CENTRAL("http://central.maven.org/maven2"),
        ALIYUN("http://maven.aliyun.com/nexus"),
        I7MC("http://ci.mengcraft.com:8080/plugin/repository/everything");

        final String repository;

        Repository(String repository) {
            this.repository = repository;
        }
    }

    public static MavenLibrary of(String description) {
        return of(Repository.CENTRAL.repository, description);
    }

    public static MavenLibrary of(String repository, String description) {
        val split = description.split(":");
        if (!(split.length == 3 || split.length == 4)) throw new IllegalArgumentException(description);
        val itr = Arrays.asList(split).iterator();
        return new MavenLibrary(repository, itr.next(), itr.next(), itr.next(), itr.hasNext() ? itr.next() : null);
    }

}
