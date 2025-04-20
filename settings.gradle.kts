pluginManagement {
    repositories {
        // 阿里云 Gradle 插件镜像
        maven {
            url = uri("https://maven.aliyun.com/repository/gradle-plugin")
        }
        // Google 官方仓库
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        // Maven Central 仓库
        mavenCentral()
        // Gradle Plugin Portal 仓库
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 添加PJSIP仓库
        maven {
            url = uri("https://github.com/pjsip/pjproject/raw/master/pjsip-apps/src/swig/java/android/app/src/main/libs")
        }
        // 阿里云公共 Maven 仓库镜像
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
        }
        // 阿里云 Google 仓库镜像
        maven {
            url = uri("https://maven.aliyun.com/repository/google")
        }
        // 阿里云 JCenter 仓库镜像（如果需要）
        maven {
            url = uri("https://maven.aliyun.com/repository/jcenter")
        }
        // Google 官方仓库
        google()
        // Maven Central 仓库
        mavenCentral()
    }
}

rootProject.name = "WuyeApp"
include(":app")
