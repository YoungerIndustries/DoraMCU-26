using UnityEngine;
using UnityEditor;

public class PlayModeScript : MonoBehaviour
{
    [MenuItem("Tools/Enter Play Mode")]
    public static void EnterPlayMode()
    {
        if (!EditorApplication.isPlaying)
        {
            EditorApplication.isPlaying = true;
            Debug.Log("Entered Play Mode");
        }
    }
}
