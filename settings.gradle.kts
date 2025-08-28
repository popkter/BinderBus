pluginManagement {
    repositories {

        // 阿里云Google镜像
        maven(
            url = "https://maven.aliyun.com/repository/google"
        )
        maven(
            url = "https://maven.aliyun.com/repository/gradle-plugin"
        )
        // 阿里云公共仓库
        maven(
            url = "https://maven.aliyun.com/repository/public"
        )
        maven(
            url = "https://maven.aliyun.com/nexus/content/groups/public/"
        )
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 阿里云Google镜像
        maven(
            url = "https://maven.aliyun.com/repository/google"
        )
        maven(
            url = "https://maven.aliyun.com/repository/gradle-plugin"
        )
        // 阿里云公共仓库
        maven(
            url = "https://maven.aliyun.com/repository/public"
        )
        google()
        mavenCentral()
    }
}
rootProject.name = "BinderBus"
include(":app")
include(":binder_bus_proxy")
