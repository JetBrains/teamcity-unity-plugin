namespace Namespace
{
    public class BuildScript
    {
        [MenuItem("Description", false, 10)]
        public static void CustomBuild()
        {
            BumpBundleVersion();
            BuildPipeline.BuildPlayer(null, "iOS", BuildTarget.iPhone, BuildOptions.Development);
        }
    }
}