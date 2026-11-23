using UnityEngine;
using UnityEditor;

public class BuildScript
{
    [MenuItem("Build/Build Windows Standalone")]
    public static void BuildWindowsStandalone()
    {
        string[] scenes = { "Assets/Scenes/MainScene.unity" };
        string buildPath = "Builds/DoraMCU.exe";

        // Ensure scenes are in EditorBuildSettings so the built player loads them
        EditorBuildSettingsScene[] settingsScenes = new EditorBuildSettingsScene[scenes.Length];
        for (int i = 0; i < scenes.Length; i++)
        {
            settingsScenes[i] = new EditorBuildSettingsScene(scenes[i], true);
        }
        EditorBuildSettings.scenes = settingsScenes;

        BuildPlayerOptions buildPlayerOptions = new BuildPlayerOptions
        {
            scenes = scenes,
            locationPathName = buildPath,
            target = BuildTarget.StandaloneWindows64,
            options = BuildOptions.None
        };

        BuildPipeline.BuildPlayer(buildPlayerOptions);
    }
}
