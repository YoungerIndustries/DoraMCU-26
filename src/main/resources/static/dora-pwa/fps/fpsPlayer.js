/**
 * First-person player controller using pointer lock API.
 * Handles movement, jumping, crouching, and camera rotation.
 */
class FpsPlayer {
    constructor(camera, canvas) {
        this.camera = camera;
        this.canvas = canvas;
        
        // Movement settings
        this.walkSpeed = 5;
        this.runSpeed = 9;
        this.crouchSpeed = 2.5;
        this.jumpForce = 8;
        this.gravity = -20;
        this.mouseSensitivity = 0.002;
        
        // State
        this.position = new THREE.Vector3(0, 1.7, 0);
        this.velocity = new THREE.Vector3(0, 0, 0);
        this.isGrounded = false;
        this.isRunning = false;
        this.isCrouching = false;
        this.standingHeight = 1.7;
        this.crouchingHeight = 0.9;
        this.currentHeight = this.standingHeight;
        
        // Rotation
        this.pitch = 0;
        this.yaw = 0;
        this.lookClamp = 80 * Math.PI / 180;
        
        // Input
        this.keys = {};
        this.actions = [];
        
        // Pointer lock
        this.isLocked = false;
        
        // Prediction
        this.lastServerPosition = null;
        this.lastServerTick = -1;
        
        this.setupInput();
    }
    
    setupInput() {
        // Keyboard
        document.addEventListener('keydown', (e) => {
            this.keys[e.code] = true;
            
            if (e.code === 'ShiftLeft') {
                this.isRunning = !this.isRunning;
            }
            if (e.code === 'KeyC') {
                this.isCrouching = !this.isCrouching;
            }
        });
        
        document.addEventListener('keyup', (e) => {
            this.keys[e.code] = false;
        });
        
        // Mouse
        this.canvas.addEventListener('click', () => {
            if (!this.isLocked) {
                this.canvas.requestPointerLock();
            }
        });
        
        document.addEventListener('pointerlockchange', () => {
            this.isLocked = document.pointerLockElement === this.canvas;
        });
        
        document.addEventListener('mousemove', (e) => {
            if (!this.isLocked) return;
            
            this.yaw -= e.movementX * this.mouseSensitivity;
            this.pitch -= e.movementY * this.mouseSensitivity;
            this.pitch = Math.max(-this.lookClamp, Math.min(this.lookClamp, this.pitch));
        });
        
        document.addEventListener('mousedown', (e) => {
            if (e.button === 0) this.actions.push('shoot');
            if (e.button === 2) this.actions.push('aim');
        });
        
        document.addEventListener('mouseup', (e) => {
            if (e.button === 0) this.actions = this.actions.filter(a => a !== 'shoot');
            if (e.button === 2) this.actions = this.actions.filter(a => a !== 'aim');
        });
        
        // Prevent context menu
        this.canvas.addEventListener('contextmenu', (e) => e.preventDefault());
    }
    
    update(deltaTime) {
        // Update height for crouching
        const targetHeight = this.isCrouching ? this.crouchingHeight : this.standingHeight;
        this.currentHeight += (targetHeight - this.currentHeight) * 10 * deltaTime;
        
        // Get speed
        let speed = this.walkSpeed;
        if (this.isCrouching) speed = this.crouchSpeed;
        else if (this.isRunning) speed = this.runSpeed;
        
        // Calculate movement direction
        const forward = new THREE.Vector3(-Math.sin(this.yaw), 0, -Math.cos(this.yaw));
        const right = new THREE.Vector3(Math.cos(this.yaw), 0, -Math.sin(this.yaw));
        
        const moveDir = new THREE.Vector3(0, 0, 0);
        
        if (this.keys['KeyW'] || this.keys['ArrowUp']) moveDir.add(forward);
        if (this.keys['KeyS'] || this.keys['ArrowDown']) moveDir.sub(forward);
        if (this.keys['KeyA'] || this.keys['ArrowLeft']) moveDir.sub(right);
        if (this.keys['KeyD'] || this.keys['ArrowRight']) moveDir.add(right);
        
        if (moveDir.length() > 0) moveDir.normalize().multiplyScalar(speed);
        
        // Apply movement
        this.velocity.x = moveDir.x;
        this.velocity.z = moveDir.z;
        
        // Jump
        if ((this.keys['Space']) && this.isGrounded) {
            this.velocity.y = Math.sqrt(-2 * this.gravity * this.jumpForce);
            this.isGrounded = false;
            this.actions.push('jump');
        }
        
        // Gravity
        this.velocity.y += this.gravity * deltaTime;
        
        // Update position
        this.position.x += this.velocity.x * deltaTime;
        this.position.y += this.velocity.y * deltaTime;
        this.position.z += this.velocity.z * deltaTime;
        
        // Ground check
        if (this.position.y <= this.currentHeight) {
            this.position.y = this.currentHeight;
            this.velocity.y = 0;
            this.isGrounded = true;
        }
        
        // Update camera
        this.camera.position.copy(this.position);
        this.camera.rotation.order = 'YXZ';
        this.camera.rotation.y = this.yaw;
        this.camera.rotation.x = this.pitch;
        
        // Clear actions that are state-based
        this.actions = this.actions.filter(a => a === 'shoot' || a === 'jump');
        if (this.isRunning) this.actions.push('sprint');
        if (this.isCrouching) this.actions.push('crouch');
    }
    
    getInput() {
        const forward = new THREE.Vector3(-Math.sin(this.yaw), 0, -Math.cos(this.yaw));
        const right = new THREE.Vector3(Math.cos(this.yaw), 0, -Math.sin(this.yaw));
        
        const move = { x: 0, y: 0, z: 0 };
        if (this.keys['KeyW'] || this.keys['ArrowUp']) { move.x += forward.x; move.z += forward.z; }
        if (this.keys['KeyS'] || this.keys['ArrowDown']) { move.x -= forward.x; move.z -= forward.z; }
        if (this.keys['KeyA'] || this.keys['ArrowLeft']) { move.x -= right.x; move.z -= right.z; }
        if (this.keys['KeyD'] || this.keys['ArrowRight']) { move.x += right.x; move.z += right.z; }
        
        const aim = { pitch: this.pitch, yaw: this.yaw };
        
        // Get actions and clear one-shot actions
        const currentActions = [...this.actions];
        this.actions = [];
        
        return { move, aim, actions: currentActions };
    }
    
    getAimDirection() {
        const dir = new THREE.Vector3(0, 0, -1);
        dir.applyQuaternion(this.camera.quaternion);
        return dir;
    }
    
    getPosition() {
        return this.position.clone();
    }
    
    setPosition(x, y, z) {
        this.position.set(x, y, z);
    }
    
    respawn(spawnPoint) {
        this.position.set(spawnPoint.x, spawnPoint.y, spawnPoint.z);
        this.velocity.set(0, 0, 0);
        this.isGrounded = false;
    }
    
    reconcile(serverPosition, tick) {
        // Simple reconciliation - snap to server position if far enough
        if (serverPosition && this.lastServerTick >= 0) {
            const dx = serverPosition.x - this.position.x;
            const dy = serverPosition.y - this.position.y;
            const dz = serverPosition.z - this.position.z;
            const dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
            
            if (dist > 0.5) {
                this.position.set(serverPosition.x, serverPosition.y, serverPosition.z);
            }
        }
        this.lastServerPosition = serverPosition;
        this.lastServerTick = tick;
    }
}
