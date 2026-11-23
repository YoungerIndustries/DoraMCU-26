using Godot;
using System.Collections.Generic;
using DoraMCU.Core;

namespace DoraMCU.UI
{
    /// <summary>
    /// Inventory UI — displays personal inventory items and equipment.
    /// Toggled with I key.
    /// </summary>
    public partial class InventoryUI : Control
    {
        Panel _mainPanel;
        VBoxContainer _contentBox;
        Label _titleLabel;
        Label _statusLabel;

        // References
        GameManager _gameManager;

        // Sample inventory items
        List<InventoryItem> _items = new();

        public override void _Ready()
        {
            _gameManager = GetTree().Root.GetNodeOrNull("GameManager") as GameManager;
            BuildUI();
            PopulateSampleItems();
            Visible = false;
        }

        void BuildUI()
        {
            SetAnchorsPreset(Control.LayoutPreset.FullRect);

            var centerContainer = new CenterContainer
            {
            };
            AddChild(centerContainer);

            _mainPanel = new Panel
            {
                Size = new Vector2(400, 450)
            };
            centerContainer.AddChild(_mainPanel);

            var panelStyle = new StyleBoxFlat
            {
                BgColor = new Color(0.05f, 0.05f, 0.1f, 0.95f),
                BorderColor = new Color(0.2f, 0.4f, 0.8f),
                BorderWidthLeft = 2,
                BorderWidthRight = 2,
                BorderWidthTop = 2,
                BorderWidthBottom = 2,
                CornerRadiusBottomLeft = 8,
                CornerRadiusTopLeft = 8,
                CornerRadiusBottomRight = 8,
                CornerRadiusTopRight = 8
            };
            _mainPanel.AddThemeStyleboxOverride("panel", panelStyle);

            _contentBox = new VBoxContainer();
            _contentBox.AddThemeConstantOverride("separation", 10);

            // Title
            _titleLabel = new Label
            {
                Text = "Inventory",
                HorizontalAlignment = HorizontalAlignment.Center
            };
            _titleLabel.AddThemeFontSizeOverride("font_size", 24);
            _titleLabel.AddThemeColorOverride("font_color", new Color(0.4f, 0.7f, 1f));
            _contentBox.AddChild(_titleLabel);

            // Scroll container for items
            var scroll = new ScrollContainer
            {
                Size = new Vector2(360, 320),
                VerticalScrollMode = ScrollContainer.ScrollMode.Auto,
                HorizontalScrollMode = ScrollContainer.ScrollMode.Disabled
            };

            var itemBox = new VBoxContainer();
            itemBox.AddThemeConstantOverride("separation", 5);

            foreach (var item in _items)
            {
                var row = new HBoxContainer();

                var nameLabel = new Label { Text = item.Name };
                nameLabel.CustomMinimumSize = new Vector2(150, 0);
                nameLabel.AddThemeColorOverride("font_color", item.Color);
                row.AddChild(nameLabel);

                var typeLabel = new Label { Text = item.Type };
                typeLabel.AddThemeColorOverride("font_color", new Color(0.6f, 0.7f, 0.9f));
                row.AddChild(typeLabel);

                var qtyLabel = new Label { Text = $"x{item.Quantity}" };
                qtyLabel.CustomMinimumSize = new Vector2(40, 0);
                qtyLabel.HorizontalAlignment = HorizontalAlignment.Right;
                row.AddChild(qtyLabel);

                itemBox.AddChild(row);
            }

            if (_items.Count == 0)
            {
                var emptyLabel = new Label
                {
                    Text = "Inventory is empty",
                    HorizontalAlignment = HorizontalAlignment.Center
                };
                emptyLabel.AddThemeColorOverride("font_color", new Color(0.5f, 0.5f, 0.5f));
                itemBox.AddChild(emptyLabel);
            }

            scroll.AddChild(itemBox);
            _contentBox.AddChild(scroll);

            // Status
            _statusLabel = new Label
            {
                Text = $"Total: {_items.Count} items",
                HorizontalAlignment = HorizontalAlignment.Center
            };
            _contentBox.AddChild(_statusLabel);

            _mainPanel.AddChild(_contentBox);
        }

        void PopulateSampleItems()
        {
            _items.Clear();
            _items.Add(new InventoryItem("Credits", "Currency", 1000, new Color(1f, 0.85f, 0.2f)));
            _items.Add(new InventoryItem("Mineral Ore", "Resource", 50, new Color(0.6f, 0.8f, 1f)));
            _items.Add(new InventoryItem("Energy Cell", "Resource", 25, new Color(0.4f, 1f, 0.4f)));
            _items.Add(new InventoryItem("Medkit", "Equipment", 3, new Color(1f, 0.4f, 0.4f)));
            _items.Add(new InventoryItem("Navigation Chart", "Document", 1, new Color(0.8f, 0.7f, 0.4f)));
        }

        public void ShowPanel()
        {
            Visible = true;
        }

        public void HidePanel()
        {
            Visible = false;
        }

        class InventoryItem
        {
            public string Name { get; set; }
            public string Type { get; set; }
            public int Quantity { get; set; }
            public Color Color { get; set; }

            public InventoryItem(string name, string type, int quantity, Color color)
            {
                Name = name;
                Type = type;
                Quantity = quantity;
                Color = color;
            }
        }
        public void TogglePanel() { Visible = !Visible; }
    }
}
