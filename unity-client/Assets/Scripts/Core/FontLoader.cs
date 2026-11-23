using UnityEngine;
using UnityEngine.UI;

namespace DoraMCU.Client.Core
{
    /// <summary>
    /// Provides a shared font instance for all UI elements.
    /// Loads the default dynamic font once and caches it.
    /// </summary>
    public class FontLoader : Singleton<FontLoader>
    {
        private Font defaultFont;

        public Font DefaultFont
        {
            get
            {
                if (defaultFont == null)
                {
                    // Try to get the built-in font
                    defaultFont = Resources.GetBuiltinResource<Font>("LegacyRuntime.ttf");
                    
                    // If that fails, try Arial.ttf
                    if (defaultFont == null)
                    {
                        defaultFont = Resources.GetBuiltinResource<Font>("Arial.ttf");
                    }
                    
                    // If still null, get any font from the UI default
                    if (defaultFont == null)
                    {
                        var tmpText = new GameObject("_TempFontLoader").AddComponent<Text>();
                        defaultFont = tmpText.font;
                        UnityEngine.Object.DestroyImmediate(tmpText.gameObject);
                    }
                    
                    Debug.Log($"[FontLoader] Loaded font: {defaultFont?.name ?? "null"}");
                }
                return defaultFont;
            }
        }

        protected override void Awake()
        {
            base.Awake();
            DontDestroyOnLoad(gameObject);
        }
    }
}
