import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.buildFeatures.PullRequests
import jetbrains.buildServer.configs.kotlin.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.buildFeatures.pullRequests
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.buildSteps.kotlinFile
import jetbrains.buildServer.configs.kotlin.project
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.version

version = "2024.03"

project {
    vcsRoot(TagReleaseVcs)
    vcsRoot(PullRequestVcs)
    vcsRoot(MasterVcs)

    buildType(ReleaseBuildConfiguration)
    buildType(PullRequestBuildConfiguration)
    buildType(MasterBuildConfiguration)
}

object TagReleaseVcs : GitVcsRoot({
    id("TeamCityUnityPlugin_TagReleaseVcs")
    name = "TagReleaseVcs"
    url = "https://github.com/JetBrains/teamcity-unity-plugin.git"
    branch = "master"
    useTagsAsBranches = true
    branchSpec = """
        +:refs/(tags/*)
        -:<default>
    """.trimIndent()
})

object ReleaseBuildConfiguration : BuildType({
    id("TeamCityUnityPlugin_ReleaseBuild")
    name = "ReleaseBuild"

    params {
        password("env.ORG_GRADLE_PROJECT_jetbrains.marketplace.token", "credentialsJSON:1a57eff8-4658-4747-a7ff-e6cdbb3dbb6e", readOnly = true)
    }

    vcs {
        root(TagReleaseVcs)
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

object PullRequestVcs : GitVcsRoot({
    id("TeamCityUnityPlugin_PullRequestVcs")
    name = "PullRequestVcs"
    url = "https://github.com/JetBrains/teamcity-unity-plugin.git"
    branchSpec = """
        -:<default>
    """.trimIndent()
})

object PullRequestBuildConfiguration : BuildType({
    id("TeamCityUnityPlugin_PullRequestBuild")
    name = "PullRequestBuild"

    val githubTokenParameter = "GITHUB_TOKEN"
    params {
        password(githubTokenParameter, "credentialsJSON:da577601-ec6c-4387-8996-e14771fe5ca2", readOnly = true)
    }

    vcs {
        root(PullRequestVcs)
    }

    features {
        pullRequests {
            vcsRootExtId = PullRequestVcs.id?.value
            provider = github {
                filterTargetBranch = "refs/heads/master"
                filterAuthorRole = PullRequests.GitHubRoleFilter.MEMBER_OR_COLLABORATOR
                authType = token {
                    token = "%$githubTokenParameter%"
                }
            }
        }
        commitStatusPublisher {
            vcsRootExtId = PullRequestVcs.id?.value
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
})

object MasterVcs : GitVcsRoot({
    id("TeamCityUnityPlugin_MasterVcs")
    name = "MasterVcs"
    url = "https://github.com/JetBrains/teamcity-unity-plugin.git"
    branch = "master"
})

object MasterBuildConfiguration : BuildType({
    id("TeamCityUnityPlugin_MasterBuild")
    name = "MasterBuild"

    allowExternalStatus = true

    vcs {
        root(MasterVcs)
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
