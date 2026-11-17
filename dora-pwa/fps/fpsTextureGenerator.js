/**
 * Procedural texture generator for the FPS web client.
 * Creates canvas-based textures for terrain, walls, concrete, etc.
 * Falls back to solid colors if canvas generation fails.
 */

class TextureGenerator {
    constructor() {
        this.cache = {};
    }

    /**
     * Generate a procedural texture and return a THREE.CanvasTexture.
     */
    generate(type, options = {}) {
        const key = `${type}_${JSON.stringify(options)}`;
        if (this.cache[key]) {
            return this.cache[key];
        }

        const generators = {
            'ground': this._generateGround,
            'grass': this._generateGrass,
            'concrete': this._generateConcrete,
            'brick': this._generateBrick,
            'metal': this._generateMetal,
            'wood': this._generateWood,
            'rock': this._generateRock,
            'asphalt': this._generateAsphalt,
            'dirt': this._generateDirt,
            'office_floor': this._generateOfficeFloor,
            'office_wall': this._generateOfficeWall,
            'ceiling': this._generateCeiling,
            'tree_bark': this._generateTreeBark,
            'leaves': this._generateLeaves,
        };

        const generator = generators[type];
        if (!generator) {
            console.warn(`[TextureGenerator] Unknown texture type: ${type}`);
            return null;
        }

        try {
            const canvas = generator.call(this, options);
            const texture = new THREE.CanvasTexture(canvas);
            texture.wrapS = THREE.RepeatWrapping;
            texture.wrapT = THREE.RepeatWrapping;
            
            if (options.repeat) {
                texture.repeat.set(options.repeat[0], options.repeat[1]);
            }
            
            this.cache[key] = texture;
            return texture;
        } catch (e) {
            console.warn(`[TextureGenerator] Failed to generate ${type}:`, e);
            return null;
        }
    }

    /**
     * Create a material with a procedural texture, falling back to solid color.
     */
    createMaterial(type, color, options = {}) {
        const texture = this.generate(type, options);
        const matOptions = {
            color: color,
            roughness: options.roughness !== undefined ? options.roughness : 0.8,
            metalness: options.metalness !== undefined ? options.metalness : 0.1,
        };
        
        if (texture) {
            matOptions.map = texture;
        }
        
        return new THREE.MeshStandardMaterial(matOptions);
    }

    // --- Individual texture generators ---

    _generateGround(options = {}) {
        const size = 256;
        const canvas = document.createElement('canvas');
        canvas.width = size;
        canvas.height = size;
        const ctx = canvas.getContext('2d');

        // Base color
        ctx.fillStyle = '#6b7a5a';
        ctx.fillRect(0, 0, size, size);

        // Add noise for ground texture
        for (let i = 0; i < 5000; i++) {
            const x = Math.random() * size;
            const y = Math.random() * size;
            const brightness = Math.random() * 40 - 20;
            const r = 107 + brightness;
            const g = 122 + brightness;
            const b = 90 + brightness;
            ctx.fillStyle = `rgb(${r},${g},${b})`;
            ctx.fillRect(x, y, 2, 2);
        }

        // Add some darker patches
        for (let i = 0; i < 20; i++) {
            const x = Math.random() * size;
            const y = Math.random() * size;
            const radius = Math.random() * 15 + 5;
            ctx.fillStyle = `rgba(80, 90, 60, ${Math.random() * 0.3})`;
            ctx.beginPath();
            ctx.arc(x, y, radius, 0, Math.PI * 2);
            ctx.fill();
        }

        return canvas;
    }

    _generateGrass(options = {}) {
        const size = 256;
        const canvas = document.createElement('canvas');
        canvas.width = size;
        canvas.height = size;
        const ctx = canvas.getContext('2d');

        // Base green
        ctx.fillStyle = '#4a7a3a';
        ctx.fillRect(0, 0, size, size);

        // Grass blades
        for (let i = 0; i < 3000; i++) {
            const x = Math.random() * size;
            const y = Math.random() * size;
            const green = 100 + Math.random() * 80;
            ctx.strokeStyle = `rgb(${30 + Math.random()*40}, ${green}, ${20 + Math.random()*30})`;
            ctx.lineWidth = 1;
            ctx.beginPath();
            ctx.moveTo(x, y);
            ctx.lineTo(x + (Math.random() - 0.5) * 4, y - Math.random() * 6 - 2);
            ctx.stroke();
        }

        return canvas;
    }

    _generateConcrete(options = {}) {
        const size = 256;
        const canvas = document.createElement('canvas');
        canvas.width = size;
        canvas.height = size;
        const ctx = canvas.getContext('2d');

        // Base gray
        ctx.fillStyle = '#8a8a8a';
        ctx.fillRect(0, 0, size, size);

        // Concrete noise
        for (let i = 0; i < 8000; i++) {
            const x = Math.random() * size;
            const y = Math.random() * size;
            const brightness = Math.random() * 30 - 15;
            const val = 138 + brightness;
            ctx.fillStyle = `rgb(${val},${val},${val})`;
            ctx.fillRect(x, y, 1, 1);
        }

        // Small cracks
        for (let i = 0; i < 5; i++) {
            ctx.strokeStyle = `rgba(60, 60, 60, ${Math.random() * 0.3 + 0.1})`;
            ctx.lineWidth = 1;
            ctx.beginPath();
            let x = Math.random() * size;
            let y = Math.random() * size;
            ctx.moveTo(x, y);
            for (let j = 0; j < 5; j++) {
                x += (Math.random() - 0.5) * 30;
                y += (Math.random() - 0.5) * 30;
                ctx.lineTo(x, y);
            }
            ctx.stroke();
        }

        return canvas;
    }

    _generateBrick(options = {}) {
        const size = 256;
        const canvas = document.createElement('canvas');
        canvas.width = size;
        canvas.height = size;
        const ctx = canvas.getContext('2d');

        // Mortar base
        ctx.fillStyle = '#b0a090';
        ctx.fillRect(0, 0, size, size);

        const brickHeight = 20;
        const brickWidth = 40;
        const mortarWidth = 3;

        for (let row = 0; row < size / brickHeight + 1; row++) {
            const offset = (row % 2) * (brickWidth / 2);
            for (let col = -1; col < size / brickWidth + 1; col++) {
                const x = col * brickWidth + offset;
                const y = row * brickHeight;
                
                // Brick color variation
                const r = 140 + Math.random() * 40;
                const g = 70 + Math.random() * 30;
                const b = 50 + Math.random() * 20;
                ctx.fillStyle = `rgb(${r},${g},${b})`;
                ctx.fillRect(x + mortarWidth, y + mortarWidth, brickWidth - mortarWidth * 2, brickHeight - mortarWidth * 2);
            }
        }

        return canvas;
    }

    _generateMetal(options = {}) {
        const size = 256;
        const canvas = document.createElement('canvas');
        canvas.width = size;
        canvas.height = size;
        const ctx = canvas.getContext('2d');

        // Base metal color
        ctx.fillStyle = '#7a7a8a';
        ctx.fillRect(0, 0, size, size);

        // Brushed metal lines
        for (let y = 0; y < size; y += 2) {
            const brightness = Math.random() * 20 - 10;
            const val = 122 + brightness;
            ctx.strokeStyle = `rgb(${val},${val},${val + 10})`;
            ctx.lineWidth = 1;
            ctx.beginPath();
            ctx.moveTo(0, y);
            ctx.lineTo(size, y);
            ctx.stroke();
        }

        // Rivets
        for (let i = 0; i < 8; i++) {
            const x = Math.random() * size;
            const y = Math.random() * size;
            ctx.fillStyle = '#5a5a6a';
            ctx.beginPath();
            ctx.arc(x, y, 3, 0, Math.PI * 2);
            ctx.fill();
            ctx.fillStyle = '#9a9aaa';
            ctx.beginPath();
            ctx.arc(x - 1, y - 1, 2, 0, Math.PI * 2);
            ctx.fill();
        }

        return canvas;
    }

    _generateWood(options = {}) {
        const size = 256;
        const canvas = document.createElement('canvas');
        canvas.width = size;
        canvas.height = size;
        const ctx = canvas.getContext('2d');

        // Base wood color
        ctx.fillStyle = '#8a6a4a';
        ctx.fillRect(0, 0, size, size);

        // Wood grain
        for (let y = 0; y < size; y++) {
            const wave = Math.sin(y * 0.05) * 10;
            const brightness = Math.sin(y * 0.1) * 15;
            const r = 138 + brightness;
            const g = 106 + brightness;
            const b = 74 + brightness;
            ctx.strokeStyle = `rgb(${r},${g},${b})`;
            ctx.lineWidth = 1;
            ctx.beginPath();
            ctx.moveTo(0, y + wave);
            ctx.lineTo(size, y + wave);
            ctx.stroke();
        }

        // Knots
        for (let i = 0; i < 3; i++) {
            const x = Math.random() * size;
            const y = Math.random() * size;
            const radius = Math.random() * 10 + 5;
            const gradient = ctx.createRadialGradient(x, y, 0, x, y, radius);
            gradient.addColorStop(0, 'rgba(60, 40, 20, 0.6)');
            gradient.addColorStop(1, 'rgba(100, 70, 40, 0)');
            ctx.fillStyle = gradient;
            ctx.beginPath();
            ctx.arc(x, y, radius, 0, Math.PI * 2);
            ctx.fill();
        }

        return canvas;
    }

    _generateRock(options = {}) {
        const size = 256;
        const canvas = document.createElement('canvas');
        canvas.width = size;
        canvas.height = size;
        const ctx = canvas.getContext('2d');

        // Base rock color
        ctx.fillStyle = '#7a7a7a';
        ctx.fillRect(0, 0, size, size);

        // Rock texture - layered noise
        for (let layer = 0; layer < 3; layer++) {
            for (let i = 0; i < 2000; i++) {
                const x = Math.random() * size;
                const y = Math.random() * size;
                const brightness = Math.random() * 50 - 25;
                const val = 122 + brightness;
                ctx.fillStyle = `rgba(${val},${val},${val}, 0.3)`;
                ctx.fillRect(x, y, Math.random() * 4 + 1, Math.random() * 4 + 1);
            }
        }

        // Cracks and crevices
        for (let i = 0; i < 10; i++) {
            ctx.strokeStyle = `rgba(50, 50, 50, ${Math.random() * 0.4 + 0.2})`;
            ctx.lineWidth = Math.random() * 2 + 1;
            ctx.beginPath();
            let x = Math.random() * size;
            let y = Math.random() * size;
            ctx.moveTo(x, y);
            for (let j = 0; j < 8; j++) {
                x += (Math.random() - 0.5) * 40;
                y += (Math.random() - 0.5) * 40;
                ctx.lineTo(x, y);
            }
            ctx.stroke();
        }

        return canvas;
    }

    _generateAsphalt(options = {}) {
        const size = 256;
        const canvas = document.createElement('canvas');
        canvas.width = size;
        canvas.height = size;
        const ctx = canvas.getContext('2d');

        // Base asphalt color
        ctx.fillStyle = '#3a3a3a';
        ctx.fillRect(0, 0, size, size);

        // Asphalt texture
        for (let i = 0; i < 10000; i++) {
            const x = Math.random() * size;
            const y = Math.random() * size;
            const brightness = Math.random() * 30 - 15;
            const val = 58 + brightness;
            ctx.fillStyle = `rgb(${val},${val},${val})`;
            ctx.fillRect(x, y, 1, 1);
        }

        // Small stones
        for (let i = 0; i < 100; i++) {
            const x = Math.random() * size;
            const y = Math.random() * size;
            const radius = Math.random() * 2 + 0.5;
            const val = 60 + Math.random() * 40;
            ctx.fillStyle = `rgb(${val},${val},${val})`;
            ctx.beginPath();
            ctx.arc(x, y, radius, 0, Math.PI * 2);
            ctx.fill();
        }

        return canvas;
    }

    _generateDirt(options = {}) {
        const size = 256;
        const canvas = document.createElement('canvas');
        canvas.width = size;
        canvas.height = size;
        const ctx = canvas.getContext('2d');

        // Base dirt color
        ctx.fillStyle = '#8a7a5a';
        ctx.fillRect(0, 0, size, size);

        // Dirt texture
        for (let i = 0; i < 6000; i++) {
            const x = Math.random() * size;
            const y = Math.random() * size;
            const brightness = Math.random() * 40 - 20;
            const r = 138 + brightness;
            const g = 122 + brightness;
            const b = 90 + brightness;
            ctx.fillStyle = `rgb(${r},${g},${b})`;
            ctx.fillRect(x, y, 2, 2);
        }

        // Small pebbles
        for (let i = 0; i < 50; i++) {
            const x = Math.random() * size;
            const y = Math.random() * size;
            const radius = Math.random() * 3 + 1;
            const val = 100 + Math.random() * 60;
            ctx.fillStyle = `rgb(${val},${val-10},${val-20})`;
            ctx.beginPath();
            ctx.arc(x, y, radius, 0, Math.PI * 2);
            ctx.fill();
        }

        return canvas;
    }

    _generateOfficeFloor(options = {}) {
        const size = 256;
        const canvas = document.createElement('canvas');
        canvas.width = size;
        canvas.height = size;
        const ctx = canvas.getContext('2d');

        // Base tile color
        ctx.fillStyle = '#6a6a7a';
        ctx.fillRect(0, 0, size, size);

        // Tile grid
        const tileSize = 64;
        ctx.strokeStyle = 'rgba(50, 50, 60, 0.5)';
        ctx.lineWidth = 2;
        for (let x = 0; x <= size; x += tileSize) {
            ctx.beginPath();
            ctx.moveTo(x, 0);
            ctx.lineTo(x, size);
            ctx.stroke();
        }
        for (let y = 0; y <= size; y += tileSize) {
            ctx.beginPath();
            ctx.moveTo(0, y);
            ctx.lineTo(size, y);
            ctx.stroke();
        }

        // Tile variation
        for (let tx = 0; tx < size; tx += tileSize) {
            for (let ty = 0; ty < size; ty += tileSize) {
                const brightness = Math.random() * 10 - 5;
                const val = 106 + brightness;
                ctx.fillStyle = `rgba(${val},${val},${val + 10}, 0.3)`;
                ctx.fillRect(tx + 1, ty + 1, tileSize - 2, tileSize - 2);
            }
        }

        return canvas;
    }

    _generateOfficeWall(options = {}) {
        const size = 256;
        const canvas = document.createElement('canvas');
        canvas.width = size;
        canvas.height = size;
        const ctx = canvas.getContext('2d');

        // Base wall color
        ctx.fillStyle = '#7a7a8a';
        ctx.fillRect(0, 0, size, size);

        // Subtle texture
        for (let i = 0; i < 4000; i++) {
            const x = Math.random() * size;
            const y = Math.random() * size;
            const brightness = Math.random() * 20 - 10;
            const val = 122 + brightness;
            ctx.fillStyle = `rgba(${val},${val},${val + 10}, 0.2)`;
            ctx.fillRect(x, y, 1, 1);
        }

        return canvas;
    }

    _generateCeiling(options = {}) {
        const size = 256;
        const canvas = document.createElement('canvas');
        canvas.width = size;
        canvas.height = size;
        const ctx = canvas.getContext('2d');

        // Base ceiling color
        ctx.fillStyle = '#8a8a9a';
        ctx.fillRect(0, 0, size, size);

        // Ceiling tile grid
        const tileSize = 64;
        ctx.strokeStyle = 'rgba(60, 60, 70, 0.4)';
        ctx.lineWidth = 2;
        for (let x = 0; x <= size; x += tileSize) {
            ctx.beginPath();
            ctx.moveTo(x, 0);
            ctx.lineTo(x, size);
            ctx.stroke();
        }
        for (let y = 0; y <= size; y += tileSize) {
            ctx.beginPath();
            ctx.moveTo(0, y);
            ctx.lineTo(size, y);
            ctx.stroke();
        }

        return canvas;
    }

    _generateTreeBark(options = {}) {
        const size = 256;
        const canvas = document.createElement('canvas');
        canvas.width = size;
        canvas.height = size;
        const ctx = canvas.getContext('2d');

        // Base bark color
        ctx.fillStyle = '#5a4a3a';
        ctx.fillRect(0, 0, size, size);

        // Bark texture - vertical lines
        for (let x = 0; x < size; x += 3) {
            const brightness = Math.random() * 30 - 15;
            const r = 90 + brightness;
            const g = 74 + brightness;
            const b = 58 + brightness;
            ctx.strokeStyle = `rgb(${r},${g},${b})`;
            ctx.lineWidth = Math.random() * 2 + 1;
            ctx.beginPath();
            ctx.moveTo(x, 0);
            for (let y = 0; y < size; y += 10) {
                ctx.lineTo(x + (Math.random() - 0.5) * 4, y);
            }
            ctx.stroke();
        }

        return canvas;
    }

    _generateLeaves(options = {}) {
        const size = 256;
        const canvas = document.createElement('canvas');
        canvas.width = size;
        canvas.height = size;
        const ctx = canvas.getContext('2d');

        // Base green
        ctx.fillStyle = '#3a6a2a';
        ctx.fillRect(0, 0, size, size);

        // Leaf clusters
        for (let i = 0; i < 500; i++) {
            const x = Math.random() * size;
            const y = Math.random() * size;
            const radius = Math.random() * 8 + 2;
            const green = 80 + Math.random() * 100;
            ctx.fillStyle = `rgba(${20 + Math.random()*40}, ${green}, ${10 + Math.random()*30}, 0.6)`;
            ctx.beginPath();
            ctx.arc(x, y, radius, 0, Math.PI * 2);
            ctx.fill();
        }

        return canvas;
    }
}

// Export as global for module usage
window.TextureGenerator = TextureGenerator;
