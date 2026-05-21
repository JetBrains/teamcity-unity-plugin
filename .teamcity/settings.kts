import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.DslContext
import jetbrains.buildServer.configs.kotlin.buildFeatures.PullRequests
import jetbrains.buildServer.configs.kotlin.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.buildFeatures.pullRequests
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.buildSteps.kotlinFile
import jetbrains.buildServer.configs.kotlin.project
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.version

version = "2026.1"

project {
    buildType(ReleaseBuildConfiguration)
    buildType(MasterBuildConfiguration)
}

object ReleaseBuildConfiguration : BuildType({
    id("TeamCityUnityPlugin_ReleaseBuild")
    name = "Unity Plugin: release build"

    params {
        password("env.ORG_GRADLE_PROJECT_jetbrains.marketplace.token", "credentialsJSON:1a57eff8-4658-4747-a7ff-e6cdbb3dbb6e", readOnly = true)
    }

    vcs {
        root(DslContext.settingsRoot)
        branchFilter = "+:tags/*"
    }

    steps {
        kotlinFile {
            name = "calculate version"
            path = "./.teamcity/steps/calculateVersion.kts"
            arguments = "%teamcity.build.branch%"
        }
        gradle {
            name = "build"
            tasks = "clean build serverPlugin"
        }
        gradle {
            name = "publish to marketplace"
            tasks = "publishPlugin"
        }
    }

    artifactRules = "+:./plugin-unity-server/build/distributions/plugin-unity-server.zip"

    triggers {
        vcs {
            branchFilter = "+:tags/v*"
        }
    }
})

object MasterBuildConfiguration : BuildType({
    id("TeamCityUnityPlugin_MasterBuild")
    name = "Unity Plugin: master and PRs"

    allowExternalStatus = true

    vcs {
        root(DslContext.settingsRoot)
    }

    val githubTokenParameter = "GITHUB_TOKEN"
    params {
        password(githubTokenParameter, "credentialsJSON:a7d02186-a589-46b0-b17b-7c1fbdd30b42", readOnly = true)
    }

    features {
        pullRequests {
            vcsRootExtId = DslContext.settingsRoot.id?.value
            provider = github {
                filterTargetBranch = "refs/heads/master"
                filterAuthorRole = PullRequests.GitHubRoleFilter.MEMBER_OR_COLLABORATOR
                authType = token {
                    token = "%$githubTokenParameter%"
                }
            }
        }
        commitStatusPublisher {
            vcsRootExtId = DslContext.settingsRoot.id?.value
            publisher = github {
                githubUrl = "https://api.github.com"
                authType = personalToken {
                    token = "%$githubTokenParameter%"
                }
            }
        }
    }

    triggers {
        vcs {
            branchFilter = "+:*"
        }
    }

    steps {
        gradle {
            name = "build"
            tasks = "clean build serverPlugin"
        }
    }

    artifactRules = "+:./plugin-unity-server/build/distributions/plugin-unity-server.zip"
})
