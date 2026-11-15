using UnityEngine;

namespace DoraMCU.Client.Battlefield
{
    /// <summary>
    /// Handles unit selection via mouse raycasting.
    /// <para>
    /// Detects clicks on unit markers, highlights the selected unit,
    /// and fires events for UI updates.
    /// </para>
    /// </summary>
    public class UnitSelector : MonoBehaviour
    {
        private GameObject selectedUnit;
        private Camera camera;

        public event System.Action<UnitInfo> OnUnitSelected;
        public event System.Action OnUnitDeselected;

        void Start()
        {
            camera = Camera.main;
        }

        void Update()
        {
            if (Input.GetMouseButtonDown(0) && !IsCameraOrbiting())
            {
                SelectUnit();
            }

            // Deselect on Escape
            if (Input.GetKeyDown(KeyCode.Escape) && selectedUnit != null)
            {
                DeselectUnit();
            }
        }

        /// <summary>
        /// Checks if the camera is currently being orbited (to avoid conflicting with selection).
        /// </summary>
        private bool IsCameraOrbiting()
        {
            return Input.GetMouseButton(0) && Input.mousePosition != GetLastClickPosition();
        }

        private Vector3 lastClickPosition;

        private Vector3 GetLastClickPosition()
        {
            return lastClickPosition;
        }

        /// <summary>
        /// Performs raycast to select a unit.
        /// </summary>
        private void SelectUnit()
        {
            lastClickPosition = Input.mousePosition;

            var ray = camera.ScreenPointToRay(Input.mousePosition);
            RaycastHit hit;

            if (Physics.Raycast(ray, out hit, 1000f))
            {
                var unitInfo = hit.collider?.GetComponent<UnitInfo>();
                if (unitInfo != null)
                {
                    // Deselect previous
                    if (selectedUnit != null)
                    {
                        UnitGenerator.HighlightUnit(selectedUnit, false);
                    }

                    // Select new unit
                    selectedUnit = hit.collider.gameObject;
                    UnitGenerator.HighlightUnit(selectedUnit, true);

                    OnUnitSelected?.Invoke(unitInfo);
                    Debug.Log($"[UnitSelector] Selected unit: {unitInfo.UnitName} ({unitInfo.UnitType})");
                }
                else
                {
                    // Clicked on non-unit object
                    DeselectUnit();
                }
            }
            else
            {
                // Clicked on empty space
                DeselectUnit();
            }
        }

        /// <summary>
        /// Deselects the current unit.
        /// </summary>
        public void DeselectUnit()
        {
            if (selectedUnit != null)
            {
                UnitGenerator.HighlightUnit(selectedUnit, false);
                selectedUnit = null;
                OnUnitDeselected?.Invoke();
            }
        }

        /// <summary>
        /// Gets the currently selected unit info.
        /// </summary>
        public UnitInfo GetSelectedUnitInfo()
        {
            return selectedUnit?.GetComponent<UnitInfo>();
        }

        /// <summary>
        /// Clears selection when battlefield is cleared.
        /// </summary>
        public void ClearSelection()
        {
            selectedUnit = null;
        }
    }
}
