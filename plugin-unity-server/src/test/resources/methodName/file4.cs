public class BuildScript {
    public static string AndroidTitle = "Android";

    [MenuItem("Custom Build")]
    public static void BuildAndroid()
    {
        BuildPlayerOptions buildPlayerOptions = new BuildPlayerOptions();
        buildPlayerOptions.scenes = new[] { "Assets/Scenes/Main/" + AndroidTitle + ".unity" };
        buildPlayerOptions.locationPathName = "MyArtifact.apk";
        buildPlayerOptions.target = BuildTarget.Android;
        buildPlayerOptions.options = BuildOptions.None;
        BuildPipeline.BuildPlayer(buildPlayerOptions);
    }

    [Obsolete("ThisClass is obsolete. Use ThisClass2 instead.")]
    static void Method1()
    {
    }

    [AttributeUsage(AttributeTargets.Class | AttributeTargets.Struct)]
    static void Method2()
    {
    }

    [Obsolete]
    static void Method3()
    {
    }
}