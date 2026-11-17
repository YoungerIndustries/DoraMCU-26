// World Map Visualization Module
// Renders the unified world map as an interactive 2D graph with nodes and edges

class WorldMapView {
    constructor(canvasElement, infoPanelElement) {
        this.canvas = canvasElement;
        this.ctx = canvasElement.getContext('2d');
        this.infoPanel = infoPanelElement;
        this.nodes = [];
        this.edges = [];
        this.selectedNode = null;
        this.hoveredNode = null;

        // Camera / viewport state
        this.cameraX = 0;
        this.cameraY = 0;
        this.zoom = 1;
        this.isDragging = false;
        this.dragStartX = 0;
        this.dragStartY = 0;
        this.cameraStartX = 0;
        this.cameraStartY = 0;

        // Node type colors
        this.typeColors = {
            'LOCATION': '#64ffda',
            'FACTION': '#ff5252',
            'PLANET': '#ffd740',
            'STAR_SYSTEM': '#7c4dff',
            'DUNGEON': '#ff9800',
            'CITY': '#4fc3f7',
            'SPACE_STATION': '#00e676',
            'TERRITORY': '#e040fb',
            'CUSTOM': '#b0bec5'
        };

        // Edge type styles
        this.edgeStyles = {
            'TRAVEL': { color: 'rgba(100,255,218,0.3)', width: 1, dash: [] },
            'HYPERLANE': { color: 'rgba(124,77,255,0.4)', width: 2, dash: [8, 4] },
            'WARP_GATE': { color: 'rgba(255,215,64,0.4)', width: 2, dash: [4, 4] },
            'COMBAT': { color: 'rgba(255,82,82,0.4)', width: 2, dash: [] },
            'DIPLOMATIC': { color: 'rgba(0,230,118,0.3)', width: 1, dash: [6, 3] }
        };

        this.resizeCanvas();
        this.bindEvents();
    }

    resizeCanvas() {
        const rect = this.canvas.parentElement.getBoundingClientRect();
        this.canvas.width = rect.width;
        this.canvas.height = rect.height;
        this.centerX = this.canvas.width / 2;
        this.centerY = this.canvas.height / 2;
    }

    bindEvents() {
        // Mouse wheel zoom
        this.canvas.addEventListener('wheel', (e) => {
            e.preventDefault();
            const delta = e.deltaY > 0 ? 0.9 : 1.1;
            this.zoom = Math.max(0.1, Math.min(10, this.zoom * delta));
            this.render();
        });

        // Mouse drag for panning
        this.canvas.addEventListener('mousedown', (e) => {
            if (e.button === 0) {
                this.isDragging = true;
                this.dragStartX = e.clientX;
                this.dragStartY = e.clientY;
                this.cameraStartX = this.cameraX;
                this.cameraStartY = this.cameraY;
                this.canvas.style.cursor = 'grabbing';
            }
        });

        this.canvas.addEventListener('mousemove', (e) => {
            if (this.isDragging) {
                this.cameraX = this.cameraStartX + (e.clientX - this.dragStartX);
                this.cameraY = this.cameraStartY + (e.clientY - this.dragStartY);
                this.render();
            } else {
                // Hover detection
                const worldPos = this.screenToWorld(e.clientX, e.clientY);
                this.hoveredNode = this.findNodeAt(worldPos.x, worldPos.y);
                this.canvas.style.cursor = this.hoveredNode ? 'pointer' : 'default';
                this.render();
            }
        });

        this.canvas.addEventListener('mouseup', () => {
            this.isDragging = false;
            this.canvas.style.cursor = 'default';
        });

        this.canvas.addEventListener('mouseleave', () => {
            this.isDragging = false;
            this.hoveredNode = null;
            this.render();
        });

        // Click to select node
        this.canvas.addEventListener('click', (e) => {
            const worldPos = this.screenToWorld(e.clientX, e.clientY);
            const node = this.findNodeAt(worldPos.x, worldPos.y);
            if (node) {
                this.selectedNode = node;
                this.showNodeInfo(node);
            } else {
                this.selectedNode = null;
                this.infoPanel.innerHTML = '<p class="map-info-empty">Click a node to see details</p>';
            }
            this.render();
        });

        // Touch support
        let lastTouchDist = 0;
        this.canvas.addEventListener('touchstart', (e) => {
            if (e.touches.length === 1) {
                this.isDragging = true;
                this.dragStartX = e.touches[0].clientX;
                this.dragStartY = e.touches[0].clientY;
                this.cameraStartX = this.cameraX;
                this.cameraStartY = this.cameraY;
            } else if (e.touches.length === 2) {
                this.isDragging = false;
                lastTouchDist = Math.hypot(
                    e.touches[0].clientX - e.touches[1].clientX,
                    e.touches[0].clientY - e.touches[1].clientY
                );
            }
            e.preventDefault();
        }, { passive: false });

        this.canvas.addEventListener('touchmove', (e) => {
            if (e.touches.length === 1 && this.isDragging) {
                this.cameraX = this.cameraStartX + (e.touches[0].clientX - this.dragStartX);
                this.cameraY = this.cameraStartY + (e.touches[0].clientY - this.dragStartY);
                this.render();
            } else if (e.touches.length === 2) {
                const dist = Math.hypot(
                    e.touches[0].clientX - e.touches[1].clientX,
                    e.touches[0].clientY - e.touches[1].clientY
                );
                const scale = dist / lastTouchDist;
                this.zoom = Math.max(0.1, Math.min(10, this.zoom * scale));
                lastTouchDist = dist;
                this.render();
            }
            e.preventDefault();
        }, { passive: false });

        this.canvas.addEventListener('touchend', (e) => {
            if (e.touches.length === 0) {
                this.isDragging = false;
            }
        });

        // Window resize
        window.addEventListener('resize', () => {
            this.resizeCanvas();
            this.render();
        });
    }

    screenToWorld(screenX, screenY) {
        return {
            x: (screenX - this.centerX - this.cameraX) / (200 * this.zoom),
            y: (screenY - this.centerY - this.cameraY) / (200 * this.zoom)
        };
    }

    worldToScreen(worldX, worldY) {
        return {
            x: worldX * 200 * this.zoom + this.centerX + this.cameraX,
            y: worldY * 200 * this.zoom + this.centerY + this.cameraY
        };
    }

    findNodeAt(x, y) {
        const radius = 12 / this.zoom;
        for (const node of this.nodes) {
            const dx = node.x - x;
            const dy = node.y - y;
            if (Math.sqrt(dx * dx + dy * dy) < radius) {
                return node;
            }
        }
        return null;
    }

    setNodes(nodes) {
        this.nodes = nodes || [];
        this.centerOnNodes();
        this.render();
    }

    setEdges(edges) {
        this.edges = edges || [];
        this.render();
    }

    centerOnNodes() {
        if (this.nodes.length === 0) return;
        let minX = Infinity, maxX = -Infinity, minY = Infinity, maxY = -Infinity;
        for (const n of this.nodes) {
            if (n.x < minX) minX = n.x;
            if (n.x > maxX) maxX = n.x;
            if (n.y < minY) minY = n.y;
            if (n.y > maxY) maxY = n.y;
        }
        const centerX = (minX + maxX) / 2;
        const centerY = (minY + maxY) / 2;
        this.cameraX = -centerX * 200 * this.zoom;
        this.cameraY = -centerY * 200 * this.zoom;

        // Auto-fit zoom
        const rangeX = maxX - minX || 1;
        const rangeY = maxY - minY || 1;
        const fitZoom = Math.min(this.canvas.width / (rangeX * 200), this.canvas.height / (rangeY * 200), 3);
        this.zoom = Math.max(0.2, fitZoom * 0.8);
        this.cameraX = -centerX * 200 * this.zoom;
        this.cameraY = -centerY * 200 * this.zoom;
    }

    showNodeInfo(node) {
        const connectedEdges = this.edges.filter(
            e => e.sourceId === node.id || e.targetId === node.id
        );
        const connections = connectedEdges.map(e => {
            const otherId = e.sourceId === node.id ? e.targetId : e.sourceId;
            const otherNode = this.nodes.find(n => n.id === otherId);
            return otherNode ? otherNode.name : otherId;
        });

        this.infoPanel.innerHTML = `
            <div class="map-node-info">
                <h3 class="map-node-name">${node.name}</h3>
                <span class="map-node-type">${node.type}</span>
                <span class="map-node-module">${node.module}</span>
                <p class="map-node-desc">${node.description || 'No description available.'}</p>
                <div class="map-node-connections">
                    <strong>Connections (${connections.length}):</strong>
                    <ul>${connections.map(c => `<li>${c}</li>`).join('')}</ul>
                </div>
                ${node.discovered ? '<span class="map-discovered">✓ Discovered</span>' : '<span class="map-undiscovered">Not yet discovered</span>'}
            </div>
        `;
    }

    render() {
        const ctx = this.ctx;
        const w = this.canvas.width;
        const h = this.canvas.height;

        // Clear background
        ctx.fillStyle = '#0a0e27';
        ctx.fillRect(0, 0, w, h);

        // Draw grid
        this.drawGrid(ctx, w, h);

        // Draw edges
        for (const edge of this.edges) {
            const source = this.nodes.find(n => n.id === edge.sourceId);
            const target = this.nodes.find(n => n.id === edge.targetId);
            if (!source || !target) continue;

            const s = this.worldToScreen(source.x, source.y);
            const t = this.worldToScreen(target.x, target.y);

            const style = this.edgeStyles[edge.type] || this.edgeStyles['TRAVEL'];

            ctx.save();
            ctx.strokeStyle = style.color;
            ctx.lineWidth = style.width * this.zoom;
            if (style.dash.length > 0) {
                ctx.setLineDash(style.dash.map(d => d * this.zoom));
            }
            ctx.beginPath();
            ctx.moveTo(s.x, s.y);
            ctx.lineTo(t.x, t.y);
            ctx.stroke();
            ctx.restore();
        }

        // Draw nodes
        for (const node of this.nodes) {
            const pos = this.worldToScreen(node.x, node.y);
            const radius = (node === this.selectedNode ? 10 : 7) * this.zoom;
            const color = this.typeColors[node.type] || '#b0bec5';

            // Glow effect for hovered/selected
            if (node === this.hoveredNode || node === this.selectedNode) {
                ctx.save();
                ctx.beginPath();
                ctx.arc(pos.x, pos.y, radius * 2.5, 0, Math.PI * 2);
                const gradient = ctx.createRadialGradient(pos.x, pos.y, radius, pos.x, pos.y, radius * 2.5);
                gradient.addColorStop(0, color + '60');
                gradient.addColorStop(1, color + '00');
                ctx.fillStyle = gradient;
                ctx.fill();
                ctx.restore();
            }

            // Node circle
            ctx.save();
            ctx.beginPath();
            ctx.arc(pos.x, pos.y, radius, 0, Math.PI * 2);
            ctx.fillStyle = node.discovered ? color : color + '80';
            ctx.fill();
            ctx.strokeStyle = node === this.selectedNode ? '#ffffff' : color;
            ctx.lineWidth = (node === this.selectedNode ? 3 : 1.5) * this.zoom;
            ctx.stroke();
            ctx.restore();

            // Node label
            const fontSize = Math.max(8, 10 * this.zoom);
            ctx.save();
            ctx.font = `${fontSize}px Inter, sans-serif`;
            ctx.fillStyle = node === this.hoveredNode || node === this.selectedNode ? '#ffffff' : '#8892b0';
            ctx.textAlign = 'center';
            ctx.textBaseline = 'top';
            ctx.fillText(node.name, pos.x, pos.y + radius + 4 * this.zoom);
            ctx.restore();
        }

        // Draw legend
        this.drawLegend(ctx, w, h);
    }

    drawGrid(ctx, w, h) {
        const gridSize = 50 * this.zoom;
        if (gridSize < 10) return;

        ctx.save();
        ctx.strokeStyle = 'rgba(42, 48, 112, 0.3)';
        ctx.lineWidth = 0.5;

        const offsetX = (this.cameraX + this.centerX) % gridSize;
        const offsetY = (this.cameraY + this.centerY) % gridSize;

        ctx.beginPath();
        for (let x = offsetX; x < w; x += gridSize) {
            ctx.moveTo(x, 0);
            ctx.lineTo(x, h);
        }
        for (let y = offsetY; y < h; y += gridSize) {
            ctx.moveTo(0, y);
            ctx.lineTo(w, y);
        }
        ctx.stroke();

        // Draw origin axes
        const origin = this.worldToScreen(0, 0);
        ctx.strokeStyle = 'rgba(100, 255, 218, 0.15)';
        ctx.lineWidth = 1;
        ctx.beginPath();
        ctx.moveTo(origin.x, 0);
        ctx.lineTo(origin.x, h);
        ctx.moveTo(0, origin.y);
        ctx.lineTo(w, origin.y);
        ctx.stroke();
        ctx.restore();
    }

    drawLegend(ctx, w, h) {
        const legendX = 16;
        const legendY = h - 20 * (Object.keys(this.typeColors).length + 1);
        const fontSize = 11;

        ctx.save();
        ctx.font = `${fontSize}px Inter, sans-serif`;

        // Background
        const boxW = 180;
        const boxH = 20 * (Object.keys(this.typeColors).length + 1);
        ctx.fillStyle = 'rgba(19, 24, 66, 0.85)';
        ctx.strokeStyle = 'rgba(42, 48, 112, 0.6)';
        ctx.lineWidth = 1;
        ctx.beginPath();
        ctx.roundRect(legendX, legendY, boxW, boxH, 6);
        ctx.fill();
        ctx.stroke();

        let y = legendY + 16;
        for (const [type, color] of Object.entries(this.typeColors)) {
            ctx.beginPath();
            ctx.arc(legendX + 12, y, 5, 0, Math.PI * 2);
            ctx.fillStyle = color;
            ctx.fill();
            ctx.fillStyle = '#8892b0';
            ctx.textAlign = 'left';
            ctx.textBaseline = 'middle';
            ctx.fillText(type, legendX + 24, y);
            y += 20;
        }
        ctx.restore();
    }

    // Filter by module
    filterByModule(module) {
        if (!module) {
            this.setNodes(this.nodes);
        } else {
            const filtered = this.nodes.filter(n => n.module === module);
            // Temporarily replace nodes for display
            const allNodes = this.nodes;
            this.nodes = filtered;
            this.centerOnNodes();
            this.nodes = allNodes;
            // Re-render with highlight
            this.render();
        }
    }

    resetView() {
        this.cameraX = 0;
        this.cameraY = 0;
        this.zoom = 1;
        this.centerOnNodes();
    }
}

// Export as global
window.WorldMapView = WorldMapView;
