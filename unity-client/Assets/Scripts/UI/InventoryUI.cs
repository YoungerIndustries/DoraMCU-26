using System.Collections.Generic;
using UnityEngine;
using UnityEngine.EventSystems;
using DoraMCU.Client.Core;
using UnityEngine.UI;

namespace DoraMCU.Client.UI
{
    /// <summary>
    /// Inventory UI for managing player items, equipment, and resources.
    /// Displays a grid-based inventory with item tooltips and drag-and-drop support.
    /// </summary>
    public class InventoryUI : Singleton<InventoryUI>
    {
        [Header("Inventory Panel")]
        [SerializeField] private GameObject inventoryPanel;
        [SerializeField] private Text inventoryTitle;
        [SerializeField] private ScrollRect inventoryScroll;
        [SerializeField] private GameObject itemGrid;

        [Header("Item Details Panel")]
        [SerializeField] private GameObject detailsPanel;
        [SerializeField] private Text itemNameText;
        [SerializeField] private Text itemTypeText;
        [SerializeField] private Text itemDescriptionText;
        [SerializeField] private Text itemQuantityText;
        [SerializeField] private Button equipButton;
        [SerializeField] private Button useButton;
        [SerializeField] private Button dropButton;

        [Header("Settings")]
        [SerializeField] private int gridColumns = 6;
        [SerializeField] private int gridRows = 6;
        [SerializeField] private float itemSize = 50f;
        [SerializeField] private float itemSpacing = 5f;
        [SerializeField] private Color inventoryBackgroundColor = new Color(0, 0, 0, 0.85f);
        [SerializeField] private Color itemSlotColor = new Color(0.15f, 0.15f, 0.25f, 0.8f);
        [SerializeField] private Color selectedItemColor = new Color(0.2f, 0.3f, 0.5f, 0.9f);

        private GameObject inventoryPanelGo;
        private GameObject detailsPanelGo;
        private GameObject gridContent;
        private List<InventoryItem> items = new();
        private InventoryItem selectedItem;
        private int selectedSlotIndex = -1;

        /// <summary>
        /// Represents an inventory item.
        /// </summary>
        public class InventoryItem
        {
            public string Id;
            public string Name;
            public string Type;
            public string Description;
            public int Quantity;
            public string IconPath;
            public bool IsEquippable;
            public bool IsEquipped;
            public Dictionary<string, object> Stats;

            public InventoryItem(string id, string name, string type, string description, int quantity)
            {
                Id = id;
                Name = name;
                Type = type;
                Description = description;
                Quantity = quantity;
                Stats = new Dictionary<string, object>();
            }
        }

        protected override void Awake()
        {
            base.Awake();
            DontDestroyOnLoad(gameObject);
        }

        void Start()
        {
            EnsureUIElements();
            HideInventory();
        }

        /// <summary>
        /// Create inventory UI elements programmatically.
        /// </summary>
        private void EnsureUIElements()
        {
            var canvas = FindObjectOfType<Canvas>();
            if (canvas == null)
            {
                var canvasGo = new GameObject("InventoryCanvas");
                canvas = canvasGo.AddComponent<Canvas>();
                canvas.renderMode = RenderMode.ScreenSpaceOverlay;
                canvasGo.AddComponent<CanvasRenderer>();
                canvasGo.AddComponent<GraphicRaycaster>();

                var eventSystem = Object.FindObjectOfType<EventSystem>();
                if (eventSystem == null)
                {
                    var esGo = new GameObject("EventSystem");
                    esGo.AddComponent<EventSystem>();
                    esGo.AddComponent<StandaloneInputModule>();
                }
            }

            if (inventoryPanel == null)
            {
                // Main inventory panel (centered)
                float panelWidth = (gridColumns * (itemSize + itemSpacing)) + 100;
                float panelHeight = (gridRows * (itemSize + itemSpacing)) + 150;

                inventoryPanelGo = new GameObject("InventoryPanel");
                inventoryPanelGo.transform.SetParent(canvas.transform);
                
                var rect = inventoryPanelGo.AddComponent<RectTransform>();
                rect.anchorMin = Vector2.zero;
                rect.anchorMax = Vector2.one;
                rect.anchoredPosition = Vector2.zero;
                rect.sizeDelta = new Vector2(-panelWidth, -panelHeight);

                var bg = inventoryPanelGo.AddComponent<Image>();
                bg.color = inventoryBackgroundColor;

                // Title
                var titleGo = new GameObject("Title");
                titleGo.transform.SetParent(inventoryPanelGo.transform);
                var titleRect = titleGo.AddComponent<RectTransform>();
                titleRect.anchorMin = new Vector2(0, 1);
                titleRect.anchorMax = new Vector2(1, 1);
                titleRect.anchoredPosition = new Vector2(0, -10);
                titleRect.sizeDelta = new Vector2(0, -30);

                var titleText = titleGo.AddComponent<Text>();
                titleText.text = "INVENTORY";
                titleText.fontSize = 24;
                titleText.font = FontLoader.Instance.DefaultFont;
                titleText.color = Color.cyan;
                titleText.fontStyle = FontStyle.Bold;
                titleText.alignment = TextAnchor.MiddleCenter;

                // Grid content area
                gridContent = new GameObject("GridContent");
                gridContent.transform.SetParent(inventoryPanelGo.transform);
                var gridRect = gridContent.AddComponent<RectTransform>();
                gridRect.anchorMin = new Vector2(0, 0);
                gridRect.anchorMax = new Vector2(0.65f, 0.85f);
                gridRect.anchoredPosition = new Vector2(0, 0);
                gridRect.sizeDelta = Vector2.zero;

                // Create grid slots
                CreateGridSlots();

                // Details panel (right side)
                detailsPanelGo = new GameObject("DetailsPanel");
                detailsPanelGo.transform.SetParent(inventoryPanelGo.transform);
                var detailsRect = detailsPanelGo.AddComponent<RectTransform>();
                detailsRect.anchorMin = new Vector2(0.65f, 0);
                detailsRect.anchorMax = new Vector2(1, 0.85f);
                detailsRect.anchoredPosition = Vector2.zero;
                detailsRect.sizeDelta = Vector2.zero;

                var detailsBg = detailsPanelGo.AddComponent<Image>();
                detailsBg.color = UIConfig.Instance.ButtonBackground;

                // Item name
                var nameGo = new GameObject("ItemName");
                nameGo.transform.SetParent(detailsPanelGo.transform);
                var nameRect = nameGo.AddComponent<RectTransform>();
                nameRect.anchorMin = new Vector2(0, 0.9f);
                nameRect.anchorMax = new Vector2(1, 1);
                nameRect.anchoredPosition = Vector2.zero;
                nameRect.sizeDelta = Vector2.zero;

                itemNameText = nameGo.AddComponent<Text>();
                itemNameText.text = "Item Name";
                itemNameText.fontSize = 18;
                itemNameText.font = FontLoader.Instance.DefaultFont;
                itemNameText.color = Color.cyan;
                itemNameText.fontStyle = FontStyle.Bold;
                itemNameText.horizontalOverflow = HorizontalWrapMode.Overflow;

                // Item type
                var typeGo = new GameObject("ItemType");
                typeGo.transform.SetParent(detailsPanelGo.transform);
                var typeRect = typeGo.AddComponent<RectTransform>();
                typeRect.anchorMin = new Vector2(0, 0.75f);
                typeRect.anchorMax = new Vector2(1, 0.85f);
                typeRect.anchoredPosition = Vector2.zero;
                typeRect.sizeDelta = Vector2.zero;

                itemTypeText = typeGo.AddComponent<Text>();
                itemTypeText.text = "Type";
                itemTypeText.fontSize = 12;
                itemTypeText.font = FontLoader.Instance.DefaultFont;
                itemTypeText.color = Color.gray;
                itemTypeText.horizontalOverflow = HorizontalWrapMode.Overflow;

                // Item description
                var descGo = new GameObject("ItemDescription");
                descGo.transform.SetParent(detailsPanelGo.transform);
                var descRect = descGo.AddComponent<RectTransform>();
                descRect.anchorMin = new Vector2(0, 0.2f);
                descRect.anchorMax = new Vector2(1, 0.7f);
                descRect.anchoredPosition = Vector2.zero;
                descRect.sizeDelta = Vector2.zero;

                itemDescriptionText = descGo.AddComponent<Text>();
                itemDescriptionText.text = "Description";
                itemDescriptionText.fontSize = 12;
                itemDescriptionText.font = FontLoader.Instance.DefaultFont;
                itemDescriptionText.color = UIConfig.Instance.TextDefault;
                itemDescriptionText.horizontalOverflow = HorizontalWrapMode.Overflow;
                itemDescriptionText.verticalOverflow = VerticalWrapMode.Overflow;

                // Item quantity
                var qtyGo = new GameObject("ItemQuantity");
                qtyGo.transform.SetParent(detailsPanelGo.transform);
                var qtyRect = qtyGo.AddComponent<RectTransform>();
                qtyRect.anchorMin = new Vector2(0, 0.1f);
                qtyRect.anchorMax = new Vector2(1, 0.2f);
                qtyRect.anchoredPosition = Vector2.zero;
                qtyRect.sizeDelta = Vector2.zero;

                itemQuantityText = qtyGo.AddComponent<Text>();
                itemQuantityText.text = "Quantity: 1";
                itemQuantityText.fontSize = 12;
                itemQuantityText.font = FontLoader.Instance.DefaultFont;
                itemQuantityText.color = UIConfig.Instance.TextDefault;
                itemQuantityText.horizontalOverflow = HorizontalWrapMode.Overflow;

                // Action buttons
                float btnY = 0f;
                float btnHeight = 0.08f;

                equipButton = CreateDetailButton(detailsPanelGo.transform, "Equip", "EQUIP", new Vector2(0, btnY), new Vector2(0.45f, btnY + btnHeight), OnEquip);
                btnY += btnHeight + 0.02f;
                useButton = CreateDetailButton(detailsPanelGo.transform, "Use", "USE", new Vector2(0, btnY), new Vector2(0.45f, btnY + btnHeight), OnUse);
                btnY += btnHeight + 0.02f;
                dropButton = CreateDetailButton(detailsPanelGo.transform, "Drop", "DROP", new Vector2(0.55f, btnY - (btnHeight * 2)), new Vector2(1, btnY + btnHeight), OnDrop);
            }
        }

        /// <summary>
        /// Create grid slots for inventory items.
        /// </summary>
        private void CreateGridSlots()
        {
            for (int row = 0; row < gridRows; row++)
            {
                for (int col = 0; col < gridColumns; col++)
                {
                    int index = row * gridColumns + col;
                    var slotGo = new GameObject($"Slot_{index}");
                    slotGo.transform.SetParent(gridContent.transform);

                    var rect = slotGo.AddComponent<RectTransform>();
                    float x = col * (itemSize + itemSpacing);
                    float y = -(row * (itemSize + itemSpacing));
                    rect.anchoredPosition = new Vector2(x, y);
                    rect.sizeDelta = new Vector2(itemSize, itemSize);

                    var bg = slotGo.AddComponent<Image>();
                    bg.color = itemSlotColor;

                    var btn = slotGo.AddComponent<Button>();
                    int slotIndex = index;
                    btn.onClick.AddListener(() => OnSlotClick(slotIndex));

                    // Item icon placeholder
                    var iconGo = new GameObject("Icon");
                    iconGo.transform.SetParent(slotGo.transform);
                    var iconRect = iconGo.AddComponent<RectTransform>();
                    iconRect.anchorMin = Vector2.zero;
                    iconRect.anchorMax = Vector2.one;
                    iconRect.sizeDelta = new Vector2(-4, -4);

                    var iconImg = iconGo.AddComponent<Image>();
                    iconImg.color = UIConfig.Instance.TextDisabled;

                    // Quantity text
                    var qtyGo = new GameObject("Quantity");
                    qtyGo.transform.SetParent(slotGo.transform);
                    var qtyRect = qtyGo.AddComponent<RectTransform>();
                    qtyRect.anchorMin = new Vector2(0.7f, 0);
                    qtyRect.anchorMax = new Vector2(1, 0.3f);
                    qtyRect.sizeDelta = Vector2.zero;

                    var qtyText = qtyGo.AddComponent<Text>();
                    qtyText.text = "";
                    qtyText.fontSize = 10;
                    qtyText.font = FontLoader.Instance.DefaultFont;
                    qtyText.color = UIConfig.Instance.TextDefault;
                    qtyText.alignment = TextAnchor.LowerRight;
                }
            }
        }

        /// <summary>
        /// Handle slot click.
        /// </summary>
        private void OnSlotClick(int slotIndex)
        {
            if (slotIndex < items.Count)
            {
                SelectItem(slotIndex);
            }
            else
            {
                ClearSelection();
            }
        }

        /// <summary>
        /// Select an item and show its details.
        /// </summary>
        private void SelectItem(int slotIndex)
        {
            selectedSlotIndex = slotIndex;
            selectedItem = items[slotIndex];

            // Update details panel
            if (itemNameText != null) itemNameText.text = selectedItem.Name;
            if (itemTypeText != null) itemTypeText.text = selectedItem.Type;
            if (itemDescriptionText != null) itemDescriptionText.text = selectedItem.Description;
            if (itemQuantityText != null) itemQuantityText.text = $"Quantity: {selectedItem.Quantity}";

            // Show/hide action buttons
            if (equipButton != null) equipButton.gameObject.SetActive(selectedItem.IsEquippable);
            if (useButton != null) useButton.gameObject.SetActive(true);
            if (dropButton != null) dropButton.gameObject.SetActive(true);

            Debug.Log($"[InventoryUI] Selected: {selectedItem.Name}");
        }

        /// <summary>
        /// Clear item selection.
        /// </summary>
        private void ClearSelection()
        {
            selectedSlotIndex = -1;
            selectedItem = null;

            if (itemNameText != null) itemNameText.text = "Item Name";
            if (itemTypeText != null) itemTypeText.text = "Type";
            if (itemDescriptionText != null) itemDescriptionText.text = "Description";
            if (itemQuantityText != null) itemQuantityText.text = "Quantity: 1";
        }

        /// <summary>
        /// Add an item to the inventory.
        /// </summary>
        public void AddItem(InventoryItem item)
        {
            // Check if item already exists (stackable)
            foreach (var existing in items)
            {
                if (existing.Id == item.Id)
                {
                    existing.Quantity += item.Quantity;
                    RefreshGrid();
                    return;
                }
            }

            items.Add(item);
            RefreshGrid();
            Debug.Log($"[InventoryUI] Added: {item.Name}");
        }

        /// <summary>
        /// Remove an item from the inventory.
        /// </summary>
        public void RemoveItem(string itemId, int quantity = 1)
        {
            for (int i = items.Count - 1; i >= 0; i--)
            {
                if (items[i].Id == itemId)
                {
                    items[i].Quantity -= quantity;
                    if (items[i].Quantity <= 0)
                    {
                        items.RemoveAt(i);
                    }
                    break;
                }
            }
            RefreshGrid();
        }

        /// <summary>
        /// Refresh the grid display.
        /// </summary>
        private void RefreshGrid()
        {
            if (gridContent == null) return;

            // Clear existing slots
            foreach (Transform child in gridContent.transform)
            {
                GameObject.Destroy(child.gameObject);
            }

            // Recreate grid
            CreateGridSlots();

            // Populate with items
            for (int i = 0; i < items.Count && i < gridRows * gridColumns; i++)
            {
                var slot = gridContent.transform.GetChild(i);
                if (slot != null)
                {
                    var iconImg = slot.Find("Icon")?.GetComponent<Image>();
                    if (iconImg != null)
                    {
                        iconImg.color = GetItemTypeColor(items[i].Type);
                    }

                    var qtyText = slot.Find("Quantity")?.GetComponent<Text>();
                    if (qtyText != null)
                    {
                        qtyText.text = items[i].Quantity > 1 ? items[i].Quantity.ToString() : "";
                    }
                }
            }
        }

        /// <summary>
        /// Get color based on item type.
        /// </summary>
        private Color GetItemTypeColor(string type)
        {
            switch (type.ToLower())
            {
                case "weapon": return UIConfig.Instance.TextError;
                case "armor": return new Color(0.25f, 0.41f, 0.88f);
                case "resource": return UIConfig.Instance.TextWarning;
                case "module": return new Color(0.8f, 0.2f, 0.8f);
                case "consumable": return UIConfig.Instance.TextSuccess;
                default: return UIConfig.Instance.TextDisabled;
            }
        }

        /// <summary>
        /// Load inventory from player data.
        /// </summary>
        public void LoadFromPlayer(DoraMCU.Client.Network.NetworkManager.PlayerInfo player)
        {
            if (player == null) return;

            items.Clear();

            // Add modules as items
            if (player.modules != null)
            {
                foreach (var kvp in player.modules)
                {
                    items.Add(new InventoryItem(
                        $"module_{kvp.Key}",
                        kvp.Key,
                        "Module",
                        $"System module: {kvp.Key}",
                        (int)kvp.Value
                    ) { IsEquippable = true });
                }
            }

            // Add raw resources
            if (player.rawResources != null)
            {
                foreach (var kvp in player.rawResources)
                {
                    items.Add(new InventoryItem(
                        $"raw_{kvp.Key}",
                        kvp.Key,
                        "Resource",
                        $"Raw resource: {kvp.Key}",
                        (int)kvp.Value
                    ));
                }
            }

            // Add refined resources
            if (player.refinedResources != null)
            {
                foreach (var kvp in player.refinedResources)
                {
                    items.Add(new InventoryItem(
                        $"refined_{kvp.Key}",
                        kvp.Key,
                        "Resource",
                        $"Refined resource: {kvp.Key}",
                        (int)kvp.Value
                    ));
                }
            }

            RefreshGrid();
        }

        #region Button Callbacks

        private void OnEquip()
        {
            if (selectedItem != null)
            {
                selectedItem.IsEquipped = !selectedItem.IsEquipped;
                Debug.Log($"[InventoryUI] {(selectedItem.IsEquipped ? "Equipped" : "Unequipped")}: {selectedItem.Name}");
            }
        }

        private void OnUse()
        {
            if (selectedItem != null)
            {
                Debug.Log($"[InventoryUI] Used: {selectedItem.Name}");
                RemoveItem(selectedItem.Id, 1);
                if (selectedSlotIndex >= 0 && selectedSlotIndex < items.Count)
                {
                    SelectItem(selectedSlotIndex);
                }
                else
                {
                    ClearSelection();
                }
            }
        }

        private void OnDrop()
        {
            if (selectedItem != null)
            {
                Debug.Log($"[InventoryUI] Dropped: {selectedItem.Name}");
                RemoveItem(selectedItem.Id, 1);
                ClearSelection();
            }
        }

        #endregion

        #region Public API

        public void ShowInventory()
        {
            if (inventoryPanelGo != null)
            {
                inventoryPanelGo.SetActive(true);
            }
        }

        public void HideInventory()
        {
            if (inventoryPanelGo != null)
            {
                inventoryPanelGo.SetActive(false);
            }
        }

        public void SetVisible(bool visible)
        {
            if (visible)
            {
                ShowInventory();
            }
            else
            {
                HideInventory();
            }
        }

        public bool IsVisible()
        {
            return inventoryPanelGo != null && inventoryPanelGo.activeSelf;
        }

        public List<InventoryItem> GetItems()
        {
            return items;
        }

        public InventoryItem GetSelectedItem()
        {
            return selectedItem;
        }

        #endregion

        #region UI Helpers

        private Button CreateDetailButton(Transform parent, string name, string text, Vector2 anchorMin, Vector2 anchorMax, System.Action onClick)
        {
            var btnGo = new GameObject(name);
            btnGo.transform.SetParent(parent);
            var rect = btnGo.AddComponent<RectTransform>();
            rect.anchorMin = anchorMin;
            rect.anchorMax = anchorMax;
            rect.anchoredPosition = Vector2.zero;
            rect.sizeDelta = Vector2.zero;

            var btn = btnGo.AddComponent<Button>();
            var img = btnGo.AddComponent<Image>();
            img.color = UIConfig.Instance.ButtonBackground;

            var textGo = new GameObject("Text");
            textGo.transform.SetParent(btnGo.transform);
            var textRect = textGo.AddComponent<RectTransform>();
            textRect.anchorMin = Vector2.zero;
            textRect.anchorMax = Vector2.one;
            textRect.anchoredPosition = Vector2.zero;
            textRect.sizeDelta = Vector2.zero;

            var t = textGo.AddComponent<Text>();
            t.text = text;
            t.fontSize = 12;
            t.font = FontLoader.Instance.DefaultFont;
            t.color = UIConfig.Instance.TextDefault;
            t.alignment = TextAnchor.MiddleCenter;

            btn.onClick.AddListener(() => onClick());
            return btn;
        }

        #endregion
    }
}
