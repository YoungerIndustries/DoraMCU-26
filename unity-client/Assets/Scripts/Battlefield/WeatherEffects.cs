using UnityEngine;

namespace DoraMCU.Client.Battlefield
{
    /// <summary>
    /// Applies weather and environmental effects based on biosphere data.
    /// <para>
    /// Controls fog, lighting, ambient color, and particle effects
    /// to match the battlefield's environmental conditions.
    /// </para>
    /// </summary>
    public class WeatherEffects : MonoBehaviour
    {
        [Header("Weather Particles")]
        [SerializeField] private GameObject rainParticle;
        [SerializeField] private GameObject snowParticle;
        [SerializeField] private GameObject sandParticle;

        private GameObject activeWeatherParticle;
        private Light directionalLight;
        private Light ambientLight;

        void Start()
        {
            EnsureLights();
        }

        /// <summary>
        /// Ensures directional and ambient lights exist.
        /// </summary>
        private void EnsureLights()
        {
            directionalLight = FindObjectOfType<Light>()?.type == LightType.Directional 
                ? FindObjectOfType<Light>() : null;

            if (directionalLight == null)
            {
                var lightGo = new GameObject("DirectionalLight");
                directionalLight = lightGo.AddComponent<Light>();
                directionalLight.type = LightType.Directional;
                directionalLight.intensity = 0.8f;
                directionalLight.color = Color.white;
                directionalLight.transform.rotation = Quaternion.Euler(50f, 0f, 0f);
            }
        }

        /// <summary>
        /// Applies all weather effects based on biosphere data.
        /// </summary>
        public void ApplyBiosphere(SceneBiosphere biosphere, SceneLighting lighting)
        {
            if (biosphere == null) return;

            ApplyFog(biosphere);
            ApplyLighting(biosphere, lighting);
            ApplyWeatherParticles(biosphere);
            ApplyAmbientColor(biosphere);
        }

        /// <summary>
        /// Applies fog based on weather conditions.
        /// </summary>
        private void ApplyFog(SceneBiosphere biosphere)
        {
            string weather = biosphere.weather?.ToLower() ?? "clear";

            switch (weather)
            {
                case "fog":
                    RenderSettings.fog = true;
                    RenderSettings.fogDensity = 0.03f;
                    RenderSettings.fogColor = new Color(0.7f, 0.7f, 0.75f);
                    break;

                case "rain":
                    RenderSettings.fog = true;
                    RenderSettings.fogDensity = 0.01f;
                    RenderSettings.fogColor = new Color(0.5f, 0.5f, 0.55f);
                    break;

                case "storm":
                    RenderSettings.fog = true;
                    RenderSettings.fogDensity = 0.02f;
                    RenderSettings.fogColor = new Color(0.3f, 0.3f, 0.35f);
                    break;

                case "snow":
                    RenderSettings.fog = true;
                    RenderSettings.fogDensity = 0.015f;
                    RenderSettings.fogColor = new Color(0.8f, 0.8f, 0.85f);
                    break;

                case "sandstorm":
                    RenderSettings.fog = true;
                    RenderSettings.fogDensity = 0.04f;
                    RenderSettings.fogColor = new Color(0.8f, 0.7f, 0.5f);
                    break;

                default:
                    // Clear weather - minimal fog
                    RenderSettings.fog = biosphere.vegetationDensity > 0.5f;
                    RenderSettings.fogDensity = 0.002f;
                    RenderSettings.fogColor = Color.gray;
                    break;
            }
        }

        /// <summary>
        /// Applies lighting based on time of day and weather.
        /// </summary>
        private void ApplyLighting(SceneBiosphere biosphere, SceneLighting lighting)
        {
            if (directionalLight == null) return;

            // Time of day affects light intensity and color
            float intensity = lighting != null ? lighting.directional : 0.8f;
            float ambient = lighting != null ? lighting.ambient : 0.6f;

            if (biosphere.isNight)
            {
                intensity *= 0.15f;
                ambient *= 0.2f;
                directionalLight.color = new Color(0.2f, 0.25f, 0.4f);
            }
            else
            {
                string weather = biosphere.weather?.ToLower() ?? "clear";
                if (weather == "storm" || weather == "rain")
                {
                    intensity *= 0.5f;
                    directionalLight.color = new Color(0.6f, 0.6f, 0.65f);
                }
                else if (weather == "fog")
                {
                    intensity *= 0.7f;
                    directionalLight.color = new Color(0.8f, 0.8f, 0.75f);
                }
                else
                {
                    directionalLight.color = Color.white;
                }
            }

            directionalLight.intensity = intensity;
            RenderSettings.ambientIntensity = ambient;

            // Sun angle
            if (lighting != null)
            {
                directionalLight.transform.rotation = Quaternion.Euler(
                    lighting.sunAngle, 0f, 0f);
            }
        }

        /// <summary>
        /// Applies ambient light color based on climate zone.
        /// </summary>
        private void ApplyAmbientColor(SceneBiosphere biosphere)
        {
            string climate = biosphere.climateZone?.ToLower() ?? "temperate";

            Color ambientColor = climate switch
            {
                "arctic" => new Color(0.6f, 0.7f, 0.8f),
                "arid" => new Color(0.8f, 0.7f, 0.5f),
                "tropical" => new Color(0.5f, 0.6f, 0.4f),
                "temperate" => new Color(0.5f, 0.5f, 0.5f),
                _ => new Color(0.5f, 0.5f, 0.5f)
            };

            RenderSettings.ambientMode = RenderFogMode.Exponential;
            RenderSettings.ambientSkyColor = ambientColor;
            RenderSettings.ambientEquatorColor = ambientColor * 0.8f;
            RenderSettings.ambientGroundColor = ambientColor * 0.6f;
        }

        /// <summary>
        /// Spawns weather particle effects.
        /// </summary>
        private void ApplyWeatherParticles(SceneBiosphere biosphere)
        {
            // Clear existing weather particles
            if (activeWeatherParticle != null)
            {
                Destroy(activeWeatherParticle);
                activeWeatherParticle = null;
            }

            string weather = biosphere.weather?.ToLower() ?? "clear";

            switch (weather)
            {
                case "rain":
                case "storm":
                    CreateRainEffect();
                    break;

                case "snow":
                    CreateSnowEffect();
                    break;

                case "sandstorm":
                    CreateSandEffect();
                    break;

                default:
                    // No weather particles for clear weather
                    break;
            }
        }

        /// <summary>
        /// Creates a rain particle effect.
        /// </summary>
        private void CreateRainEffect()
        {
            if (rainParticle != null)
            {
                activeWeatherParticle = Instantiate(rainParticle, transform);
                return;
            }

            // Create simple rain effect using a large transparent plane
            var rainGo = new GameObject("RainEffect");
            rainGo.transform.SetParent(transform);
            rainGo.transform.localPosition = Vector3.zero;

            var renderer = rainGo.AddComponent<MeshRenderer>();
            var mat = new Material(Shader.Find("Particles/Alpha Blended"));
            mat.color = new Color(0.6f, 0.6f, 0.7f, 0.3f);
            renderer.material = mat;

            var filter = rainGo.AddComponent<MeshFilter>();
            var plane = GameObject.CreatePrimitive(PrimitiveType.Plane);
            filter.mesh = plane.GetComponent<MeshFilter>().sharedMesh;
            rainGo.transform.localScale = new Vector3(500f, 500f, 500f);
            Destroy(plane);

            activeWeatherParticle = rainGo;
        }

        /// <summary>
        /// Creates a snow particle effect.
        /// </summary>
        private void CreateSnowEffect()
        {
            if (snowParticle != null)
            {
                activeWeatherParticle = Instantiate(snowParticle, transform);
                return;
            }

            var snowGo = new GameObject("SnowEffect");
            snowGo.transform.SetParent(transform);

            var renderer = snowGo.AddComponent<MeshRenderer>();
            var mat = new Material(Shader.Find("Particles/Alpha Blended"));
            mat.color = new Color(1f, 1f, 1f, 0.4f);
            renderer.material = mat;

            var filter = snowGo.AddComponent<MeshFilter>();
            var plane = GameObject.CreatePrimitive(PrimitiveType.Plane);
            filter.mesh = plane.GetComponent<MeshFilter>().sharedMesh;
            snowGo.transform.localScale = new Vector3(500f, 500f, 500f);
            Destroy(plane);

            activeWeatherParticle = snowGo;
        }

        /// <summary>
        /// Creates a sand particle effect.
        /// </summary>
        private void CreateSandEffect()
        {
            if (sandParticle != null)
            {
                activeWeatherParticle = Instantiate(sandParticle, transform);
                return;
            }

            var sandGo = new GameObject("SandEffect");
            sandGo.transform.SetParent(transform);

            var renderer = sandGo.AddComponent<MeshRenderer>();
            var mat = new Material(Shader.Find("Particles/Alpha Blended"));
            mat.color = new Color(0.8f, 0.7f, 0.5f, 0.5f);
            renderer.material = mat;

            var filter = sandGo.AddComponent<MeshFilter>();
            var plane = GameObject.CreatePrimitive(PrimitiveType.Plane);
            filter.mesh = plane.GetComponent<MeshFilter>().sharedMesh;
            sandGo.transform.localScale = new Vector3(500f, 500f, 500f);
            Destroy(plane);

            activeWeatherParticle = sandGo;
        }

        /// <summary>
        /// Clears all weather effects.
        /// </summary>
        public void ClearWeather()
        {
            if (activeWeatherParticle != null)
            {
                Destroy(activeWeatherParticle);
                activeWeatherParticle = null;
            }

            RenderSettings.fog = false;
            RenderSettings.fogDensity = 0f;

            if (directionalLight != null)
            {
                directionalLight.intensity = 0.8f;
                directionalLight.color = Color.white;
            }

            RenderSettings.ambientIntensity = 0.6f;
        }

        void OnDestroy()
        {
            ClearWeather();
        }
    }
}
