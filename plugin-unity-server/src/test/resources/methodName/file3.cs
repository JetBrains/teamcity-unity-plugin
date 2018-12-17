class WebGLBuilder {
    static void build() {

        // Place all your scenes here
        string[] scenes = {"Assets/scenes/S_MainMenu.unity",
                            "Assets/scenes/S_Login.unity",
                            "Assets/scenes/S_Help.unity",
                            "Assets/scenes/S_1.unity",
                            "Assets/scenes/S_Reward.unity",
                            "Assets/scenes/S_Credits.unity",
                            "Assets/scenes/S_Settings.unity",
                            "Assets/scenes/S_SceneSelector.unity"};

        string pathToDeploy = "builds/WebGLversion/";

        BuildPipeline.BuildPlayer(scenes, pathToDeploy, BuildTarget.WebGL, BuildOptions.None);
    }

    static int Main1() {
        return 1;
    }

    static void Main2(String[] arguments) {
    }
}