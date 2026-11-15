/**
 * Weapon system for the web FPS client.
 * Handles weapon switching, firing, and ammo management.
 */
class FpsWeapon {
    constructor(scene, camera) {
        this.scene = scene;
        this.camera = camera;
        
        // Weapon definitions
        this.weapons = [
            { name: 'Pistol', damage: 25, fireRate: 4, reloadTime: 1.5, magSize: 12, reserveAmmo: 60, hitScan: true },
            { name: 'Assault Rifle', damage: 20, fireRate: 10, reloadTime: 2.5, magSize: 30, reserveAmmo: 120, hitScan: true },
            { name: 'Shotgun', damage: 50, fireRate: 1.5, reloadTime: 3, magSize: 6, reserveAmmo: 30, hitScan: true },
            { name: 'Machine Gun', damage: 15, fireRate: 15, reloadTime: 3.5, magSize: 50, reserveAmmo: 200, hitScan: true },
            { name: 'Sniper', damage: 80, fireRate: 1, reloadTime: 3, magSize: 5, reserveAmmo: 20, hitScan: true },
            { name: 'Rocket Launcher', damage: 100, fireRate: 0.5, reloadTime: 4, magSize: 2, reserveAmmo: 8, hitScan: false, projectileSpeed: 50 }
        ];
        
        this.currentWeaponIndex = 0;
        this.currentAmmo = this.weapons[0].magSize;
        this.currentReserve = this.weapons[0].reserveAmmo;
        this.fireCooldown = 0;
        this.isReloading = false;
        this.reloadProgress = 0;
        
        // Visual
        this.weaponGroup = new THREE.Group();
        this.scene.add(this.weaponGroup);
        this.createWeaponModel();
        
        // Muzzle flash
        this.muzzleFlash = null;
        this.createMuzzleFlash();
        
        // Callbacks
        this.onAmmoChanged = null;
        this.onWeaponChanged = null;
        this.onReloadStarted = null;
    }
    
    createWeaponModel() {
        // Simple gun model attached to camera
        const gunGeo = new THREE.BoxGeometry(0.08, 0.15, 0.5);
        const gunMat = new THREE.MeshStandardMaterial({ color: 0x333333 });
        const gun = new THREE.Mesh(gunGeo, gunMat);
        gun.position.set(0.3, -0.25, -0.5);
        this.weaponGroup.add(gun);
        
        // Barrel
        const barrelGeo = new THREE.BoxGeometry(0.04, 0.04, 0.3);
        const barrel = new THREE.Mesh(barrelGeo, gunMat);
        barrel.position.set(0.3, -0.2, -0.8);
        this.weaponGroup.add(barrel);
    }
    
    createMuzzleFlash() {
        const flashGeo = new THREE.PlaneGeometry(0.3, 0.3);
        const flashMat = new THREE.MeshBasicMaterial({ 
            color: 0xffaa00, 
            transparent: true, 
            opacity: 0,
            side: THREE.DoubleSide
        });
        this.muzzleFlash = new THREE.Mesh(flashGeo, flashMat);
        this.muzzleFlash.position.set(0.3, -0.2, -1.0);
        this.weaponGroup.add(this.muzzleFlash);
    }
    
    update(deltaTime) {
        // Attach weapon to camera
        this.weaponGroup.position.copy(this.camera.position);
        this.weaponGroup.quaternion.copy(this.camera.quaternion);
        
        // Update fire cooldown
        if (this.fireCooldown > 0) {
            this.fireCooldown -= deltaTime;
        }
        
        // Update reload
        if (this.isReloading) {
            this.reloadProgress += deltaTime;
            const weapon = this.weapons[this.currentWeaponIndex];
            if (this.reloadProgress >= weapon.reloadTime) {
                this.finishReload();
            }
        }
        
        // Hide muzzle flash
        if (this.muzzleFlash) {
            this.muzzleFlash.material.opacity *= 0.9;
        }
    }
    
    fire() {
        const weapon = this.weapons[this.currentWeaponIndex];
        
        if (this.isReloading || this.fireCooldown > 0 || this.currentAmmo <= 0) {
            if (this.currentAmmo <= 0 && this.currentReserve > 0) {
                this.reload();
            }
            return false;
        }
        
        this.currentAmmo--;
        this.fireCooldown = 1 / weapon.fireRate;
        
        // Show muzzle flash
        if (this.muzzleFlash) {
            this.muzzleFlash.material.opacity = 1;
            this.muzzleFlash.lookAt(this.camera.position);
        }
        
        if (this.onAmmoChanged) {
            this.onAmmoChanged(this.currentAmmo, this.currentReserve);
        }
        
        // Send fire to server
        if (window.fpsNetwork) {
            const aimDir = this.getAimDirection();
            const origin = this.camera.position.clone();
            
            if (weapon.hitScan) {
                // Raycast for hit detection
                const hit = this.raycast();
                if (hit) {
                    window.fpsNetwork.sendHit(hit.point, hit.playerId);
                }
            } else {
                // Projectile
                window.fpsNetwork.sendProjectile(
                    { x: origin.x, y: origin.y, z: origin.z },
                    { x: aimDir.x * weapon.projectileSpeed, y: aimDir.y * weapon.projectileSpeed, z: aimDir.z * weapon.projectileSpeed }
                );
            }
        }
        
        return true;
    }
    
    raycast() {
        // Simple raycast against remote player meshes
        const raycaster = new THREE.Raycaster();
        raycaster.setFromCamera(new THREE.Vector2(0, 0), this.camera);
        
        // Check against remote players
        if (window.fpsGame && window.fpsGame.remotePlayers) {
            const meshes = [];
            for (const [id, player] of window.fpsGame.remotePlayers) {
                if (player.mesh) meshes.push(player.mesh);
            }
            
            if (meshes.length > 0) {
                const intersects = raycaster.intersectObjects(meshes, true);
                if (intersects.length > 0 && intersects[0].distance < 100) {
                    // Find which player was hit
                    let hitPlayer = null;
                    for (const [id, player] of window.fpsGame.remotePlayers) {
                        if (player.mesh === intersects[0].object || player.mesh === intersects[0].object.parent) {
                            hitPlayer = id;
                            break;
                        }
                    }
                    return { point: intersects[0].point, playerId: hitPlayer };
                }
            }
        }
        
        return null;
    }
    
    reload() {
        const weapon = this.weapons[this.currentWeaponIndex];
        if (this.isReloading || this.currentAmmo === weapon.magSize || this.currentReserve <= 0) {
            return;
        }
        
        this.isReloading = true;
        this.reloadProgress = 0;
        if (this.onReloadStarted) this.onReloadStarted(true);
    }
    
    finishReload() {
        const weapon = this.weapons[this.currentWeaponIndex];
        const needed = weapon.magSize - this.currentAmmo;
        const available = Math.min(needed, this.currentReserve);
        
        this.currentAmmo += available;
        this.currentReserve -= available;
        this.isReloading = false;
        this.reloadProgress = 0;
        
        if (this.onReloadStarted) this.onReloadStarted(false);
        if (this.onAmmoChanged) this.onAmmoChanged(this.currentAmmo, this.currentReserve);
    }
    
    switchWeapon(index) {
        if (index < 0 || index >= this.weapons.length) return;
        
        this.currentWeaponIndex = index;
        this.currentAmmo = this.weapons[index].magSize;
        this.currentReserve = this.weapons[index].reserveAmmo;
        this.isReloading = false;
        this.fireCooldown = 0;
        
        if (this.onWeaponChanged) this.onWeaponChanged(this.weapons[index].name);
        if (this.onAmmoChanged) this.onAmmoChanged(this.currentAmmo, this.currentReserve);
    }
    
    getAimDirection() {
        const dir = new THREE.Vector3(0, 0, -1);
        dir.applyQuaternion(this.camera.quaternion);
        return dir;
    }
    
    getCurrentWeapon() {
        return this.weapons[this.currentWeaponIndex];
    }
    
    setAmmo(ammo, reserve) {
        this.currentAmmo = ammo;
        this.currentReserve = reserve;
        if (this.onAmmoChanged) this.onAmmoChanged(this.currentAmmo, this.currentReserve);
    }
}
