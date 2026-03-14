package party.morino.kerria.paper

import io.papermc.paper.plugin.loader.PluginClasspathBuilder
import io.papermc.paper.plugin.loader.PluginLoader
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.repository.RemoteRepository

@Suppress("unused")
class KerriaLoader : PluginLoader {
    override fun classloader(classpathBuilder: PluginClasspathBuilder) {
        val resolver = MavenLibraryResolver()
        resolver.addDependency(Dependency(DefaultArtifact("org.jetbrains.kotlin:kotlin-stdlib:2.3.10"), null))
        resolver.addRepository(
            RemoteRepository.Builder("paper", "default", "https://repo.papermc.io/repository/maven-public/").build(),
        )
        classpathBuilder.addLibrary(resolver)
    }
}
