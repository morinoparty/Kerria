package party.morino.kerria.addons.quickshop;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * QuickShop-Hikari アドオンのローダークラス。
 * ビルド時に生成したライブラリ一覧 (Kotlin 標準ライブラリ) を Paper に実行時に取得させる。
 *
 * Kotlin 標準ライブラリ自体をここで取得するため、このクラスは Kotlin ではなく Java で記述する。
 * Kerria 本体とは別のクラスローダーになるため、Kerria の API には Java の型だけを受け渡すこと。
 */
@SuppressWarnings("unused")
public class QuickShopAddonLoader implements PluginLoader {
    // build.gradle.kts の generateAddonLibraries タスクが生成するリソース
    private static final String LIBRARIES_RESOURCE = "addon-libraries.txt";

    // Paper が推奨する Maven Central のミラー (Maven Central を直接参照すると Paper に警告される)
    private static final String MAVEN_CENTRAL_MIRROR = "https://maven-central.storage-download.googleapis.com/maven2";

    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(LIBRARIES_RESOURCE)) {
            if (stream == null) {
                throw new IllegalStateException(LIBRARIES_RESOURCE + " is not found in the plugin jar");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            // 1 行に 1 つの Maven 座標 (group:artifact:version) が書かれている
            reader.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .forEach(coordinate -> resolver.addDependency(new Dependency(new DefaultArtifact(coordinate), null)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        resolver.addRepository(new RemoteRepository.Builder("central", "default", MAVEN_CENTRAL_MIRROR).build());

        classpathBuilder.addLibrary(resolver);
    }
}
