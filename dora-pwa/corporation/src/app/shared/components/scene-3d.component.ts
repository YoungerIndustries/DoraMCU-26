import { Component, OnInit, OnDestroy, ElementRef, ViewChild, Input, Output, EventEmitter } from '@angular/core';
import * as THREE from 'three';

export interface Scene3DConfig {
  backgroundColor?: number;
  fogColor?: number;
  fogNear?: number;
  fogFar?: number;
  cameraPosition?: { x: number; y: number; z: number };
  cameraTarget?: { x: number; y: number; z: number };
  theme?: 'space' | 'dungeon' | 'planet' | 'bridge' | 'default';
}

export interface SceneObject {
  id: string;
  position: { x: number; y: number; z: number };
  type: 'sphere' | 'box' | 'cone' | 'torus' | 'cylinder' | 'group';
  size?: { x: number; y: number; z: number };
  color?: number;
  emissive?: number;
  emissiveIntensity?: number;
  interactive?: boolean;
  userData?: Record<string, any>;
}

@Component({
  selector: 'app-scene-3d',
  standalone: true,
  imports: [],
  template: `
    <div #container class="scene-3d-container"></div>
  `,
  styles: `
    .scene-3d-container {
      width: 100%;
      height: 100%;
      position: relative;
      overflow: hidden;
    }
    .scene-3d-container canvas {
      display: block;
      width: 100% !important;
      height: 100% !important;
    }
  `,
})
export class Scene3DComponent implements OnInit, OnDestroy {
  @ViewChild('container', { static: true }) container!: ElementRef<HTMLDivElement>;

  @Input() config: Scene3DConfig = {
    backgroundColor: 0x000011,
    fogColor: 0x000011,
    fogNear: 50,
    fogFar: 200,
    cameraPosition: { x: 0, y: 10, z: 20 },
    cameraTarget: { x: 0, y: 0, z: 0 },
    theme: 'space',
  };

  @Output() objectClick = new EventEmitter<{ id: string; userData?: Record<string, any> }>();
  @Output() objectHover = new EventEmitter<{ id: string | null; userData?: Record<string, any> }>();

  private scene: THREE.Scene | null = null;
  private camera: THREE.PerspectiveCamera | null = null;
  private renderer: THREE.WebGLRenderer | null = null;
  private animationId: number | null = null;
  private objects: Map<string, THREE.Object3D> = new Map();
  private interactiveObjects: THREE.Object3D[] = [];
  private raycaster: THREE.Raycaster = new THREE.Raycaster();
  private mouse: THREE.Vector2 = new THREE.Vector2();
  private particles: THREE.Points | null = null;

  // Orbit controls (manual implementation to avoid external dependency)
  private orbitState = {
    isDragging: false,
    previousMouse: { x: 0, y: 0 },
    spherical: { theta: 0, phi: Math.PI / 4, radius: 25 },
    target: new THREE.Vector3(0, 0, 0),
  };

  ngOnInit(): void {
    this.init();
  }

  ngOnDestroy(): void {
    this.destroy();
  }

  init(): void {
    const el = this.container.nativeElement;
    if (!el) return;

    // Scene
    this.scene = new THREE.Scene();
    this._setupBackground();

    // Camera
    const width = el.clientWidth || 800;
    const height = el.clientHeight || 600;
    this.camera = new THREE.PerspectiveCamera(60, width / height, 0.1, 1000);
    const cam = this.config.cameraPosition || { x: 0, y: 10, z: 20 };
    this.camera.position.set(cam.x, cam.y, cam.z);
    const tgt = this.config.cameraTarget || { x: 0, y: 0, z: 0 };
    this.orbitState.target.set(tgt.x, tgt.y, tgt.z);
    this.camera.lookAt(this.orbitState.target);

    // Renderer
    this.renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
    this.renderer.setSize(width, height);
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    this.renderer.shadowMap.enabled = true;
    el.appendChild(this.renderer.domElement);

    // Lights
    this._setupLights();

    // Starfield
    this._createStarfield();

    // Events
    this._setupEvents(el);

    // Animation loop
    this._setupAnimationLoop();
  }

  private _setupBackground(): void {
    if (!this.scene) return;
    const bg = this.config.backgroundColor ?? 0x000011;
    this.scene.background = new THREE.Color(bg);
    if (this.config.fogColor !== undefined) {
      this.scene.fog = new THREE.Fog(
        this.config.fogColor,
        this.config.fogNear ?? 50,
        this.config.fogFar ?? 200
      );
    }
  }

  private _setupLights(): void {
    if (!this.scene) return;

    const ambient = new THREE.AmbientLight(0x404060, 0.6);
    this.scene.add(ambient);

    const directional = new THREE.DirectionalLight(0xffffff, 0.8);
    directional.position.set(5, 10, 7);
    this.scene.add(directional);

    const point = new THREE.PointLight(0x4488ff, 0.5, 50);
    point.position.set(-5, 5, -5);
    this.scene.add(point);
  }

  private _createStarfield(): void {
    if (!this.scene) return;

    const count = 2000;
    const geometry = new THREE.BufferGeometry();
    const positions = new Float32Array(count * 3);
    const colors = new Float32Array(count * 3);

    for (let i = 0; i < count; i++) {
      const i3 = i * 3;
      const radius = 50 + Math.random() * 150;
      const theta = Math.random() * Math.PI * 2;
      const phi = Math.acos(2 * Math.random() - 1);
      positions[i3] = radius * Math.sin(phi) * Math.cos(theta);
      positions[i3 + 1] = radius * Math.sin(phi) * Math.sin(theta);
      positions[i3 + 2] = radius * Math.cos(phi);

      const color = 0.5 + Math.random() * 0.5;
      colors[i3] = color;
      colors[i3 + 1] = color;
      colors[i3 + 2] = color + Math.random() * 0.2;
    }

    geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
    geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3));

    const material = new THREE.PointsMaterial({
      size: 0.3,
      vertexColors: true,
      transparent: true,
      opacity: 0.8,
    });

    this.particles = new THREE.Points(geometry, material);
    this.scene.add(this.particles);
  }

  private _setupEvents(el: HTMLDivElement): void {
    el.addEventListener('mousedown', (e) => this._onMouseDown(e));
    el.addEventListener('mousemove', (e) => this._onMouseMove(e));
    el.addEventListener('mouseup', () => this._onMouseUp());
    el.addEventListener('wheel', (e) => this._onWheel(e), { passive: false });
    el.addEventListener('click', (e) => this._onClick(e));
    el.addEventListener('mouseleave', () => {
      this._onMouseUp();
      this.objectHover.emit({ id: null });
    });
    window.addEventListener('resize', () => this._onResize());
  }

  private _onMouseDown(e: MouseEvent): void {
    this.orbitState.isDragging = true;
    this.orbitState.previousMouse = { x: e.clientX, y: e.clientY };
  }

  private _onMouseMove(e: MouseEvent): void {
    const el = this.container.nativeElement;
    const rect = el.getBoundingClientRect();
    this.mouse.x = ((e.clientX - rect.left) / rect.width) * 2 - 1;
    this.mouse.y = -((e.clientY - rect.top) / rect.height) * 2 + 1;

    if (this.orbitState.isDragging) {
      const dx = e.clientX - this.orbitState.previousMouse.x;
      const dy = e.clientY - this.orbitState.previousMouse.y;
      this.orbitState.spherical.theta -= dx * 0.005;
      this.orbitState.spherical.phi = Math.max(
        0.1,
        Math.min(Math.PI - 0.1, this.orbitState.spherical.phi + dy * 0.005)
      );
      this.orbitState.previousMouse = { x: e.clientX, y: e.clientY };
      this._updateCameraFromSpherical();
    }
  }

  private _onMouseUp(): void {
    this.orbitState.isDragging = false;
  }

  private _onWheel(e: WheelEvent): void {
    e.preventDefault();
    this.orbitState.spherical.radius = Math.max(
      5,
      Math.min(100, this.orbitState.spherical.radius + e.deltaY * 0.02)
    );
    this._updateCameraFromSpherical();
  }

  private _onClick(e: MouseEvent): void {
    if (!this.camera || !this.scene) return;

    const el = this.container.nativeElement;
    const rect = el.getBoundingClientRect();
    this.mouse.x = ((e.clientX - rect.left) / rect.width) * 2 - 1;
    this.mouse.y = -((e.clientY - rect.top) / rect.height) * 2 + 1;

    this.raycaster.setFromCamera(this.mouse, this.camera);
    const intersects = this.raycaster.intersectObjects(this.interactiveObjects, true);

    if (intersects.length > 0) {
      let obj = intersects[0].object;
      while (obj.parent && !(obj as any).userData?.id) {
        obj = obj.parent as THREE.Object3D;
      }
      const userData = (obj as any).userData || {};
      this.objectClick.emit({ id: userData.id || '', userData: userData.data || {} });
    }
  }

  private _updateCameraFromSpherical(): void {
    if (!this.camera) return;
    const s = this.orbitState.spherical;
    this.camera.position.set(
      this.orbitState.target.x + s.radius * Math.sin(s.phi) * Math.sin(s.theta),
      this.orbitState.target.y + s.radius * Math.cos(s.phi),
      this.orbitState.target.z + s.radius * Math.sin(s.phi) * Math.cos(s.theta)
    );
    this.camera.lookAt(this.orbitState.target);
  }

  private _onResize(): void {
    const el = this.container.nativeElement;
    if (!el || !this.camera || !this.renderer) return;
    const width = el.clientWidth;
    const height = el.clientHeight;
    this.camera.aspect = width / height;
    this.camera.updateProjectionMatrix();
    this.renderer.setSize(width, height);
  }

  private _setupAnimationLoop(): void {
    const animate = () => {
      this.animationId = requestAnimationFrame(animate);

      // Slowly rotate starfield
      if (this.particles) {
        this.particles.rotation.y += 0.0001;
      }

      // Hover detection
      if (this.camera && this.scene && this.interactiveObjects.length > 0) {
        this.raycaster.setFromCamera(this.mouse, this.camera);
        const intersects = this.raycaster.intersectObjects(this.interactiveObjects, true);
        if (intersects.length > 0) {
          let obj = intersects[0].object;
          while (obj.parent && !(obj as any).userData?.id) {
            obj = obj.parent as THREE.Object3D;
          }
          const userData = (obj as any).userData || {};
          this.objectHover.emit({ id: userData.id || null, userData: userData.data || {} });
        } else {
          this.objectHover.emit({ id: null });
        }
      }

      this.renderer?.render(this.scene!, this.camera!);
    };
    animate();
  }

  // Public API

  addSphere(id: string, position: { x: number; y: number; z: number }, radius: number, color: number, userData?: Record<string, any>): void {
    if (!this.scene) return;
    const geo = new THREE.SphereGeometry(radius, 16, 16);
    const mat = new THREE.MeshStandardMaterial({ color });
    const mesh = new THREE.Mesh(geo, mat);
    mesh.position.set(position.x, position.y, position.z);
    (mesh as any).userData = { id, data: userData };
    this.scene.add(mesh);
    this.objects.set(id, mesh);
    if (userData && 'interactive' in userData && userData['interactive']) {
      this.interactiveObjects.push(mesh);
    }
  }

  addBox(id: string, position: { x: number; y: number; z: number }, size: { x: number; y: number; z: number }, color: number, userData?: Record<string, any>): void {
    if (!this.scene) return;
    const geo = new THREE.BoxGeometry(size.x, size.y, size.z);
    const mat = new THREE.MeshStandardMaterial({ color });
    const mesh = new THREE.Mesh(geo, mat);
    mesh.position.set(position.x, position.y, position.z);
    (mesh as any).userData = { id, data: userData };
    this.scene.add(mesh);
    this.objects.set(id, mesh);
    if (userData && 'interactive' in userData && userData['interactive']) {
      this.interactiveObjects.push(mesh);
    }
  }

  addCone(id: string, position: { x: number; y: number; z: number }, radius: number, height: number, color: number, userData?: Record<string, any>): void {
    if (!this.scene) return;
    const geo = new THREE.ConeGeometry(radius, height, 16);
    const mat = new THREE.MeshStandardMaterial({ color });
    const mesh = new THREE.Mesh(geo, mat);
    mesh.position.set(position.x, position.y, position.z);
    (mesh as any).userData = { id, data: userData };
    this.scene.add(mesh);
    this.objects.set(id, mesh);
    if (userData && 'interactive' in userData && userData['interactive']) {
      this.interactiveObjects.push(mesh);
    }
  }

  addTorus(id: string, position: { x: number; y: number; z: number }, radius: number, tube: number, color: number, userData?: Record<string, any>): void {
    if (!this.scene) return;
    const geo = new THREE.TorusGeometry(radius, tube, 16, 32);
    const mat = new THREE.MeshStandardMaterial({ color });
    const mesh = new THREE.Mesh(geo, mat);
    mesh.position.set(position.x, position.y, position.z);
    (mesh as any).userData = { id, data: userData };
    this.scene.add(mesh);
    this.objects.set(id, mesh);
    if (userData && 'interactive' in userData && userData['interactive']) {
      this.interactiveObjects.push(mesh);
    }
  }

  addGroup(id: string, position: { x: number; y: number; z: number }, userData?: Record<string, any>): THREE.Group {
    if (!this.scene) return new THREE.Group();
    const group = new THREE.Group();
    group.position.set(position.x, position.y, position.z);
    (group as any).userData = { id, data: userData };
    this.scene.add(group);
    this.objects.set(id, group);
    if (userData && 'interactive' in userData && userData['interactive']) {
      this.interactiveObjects.push(group);
    }
    return group;
  }

  removeObject(id: string): void {
    const obj = this.objects.get(id);
    if (obj && this.scene) {
      this.scene.remove(obj);
      const idx = this.interactiveObjects.indexOf(obj);
      if (idx >= 0) this.interactiveObjects.splice(idx, 1);
      this.objects.delete(id);
    }
  }

  clearAll(): void {
    if (!this.scene) return;
    this.objects.forEach((obj) => {
      this.scene!.remove(obj);
    });
    this.objects.clear();
    this.interactiveObjects = [];
  }

  setCameraPosition(x: number, y: number, z: number): void {
    if (!this.camera) return;
    this.camera.position.set(x, y, z);
    this.camera.lookAt(this.orbitState.target);
  }

  setCameraTarget(x: number, y: number, z: number): void {
    this.orbitState.target.set(x, y, z);
  }

  getScene(): THREE.Scene | null {
    return this.scene;
  }

  getRenderer(): THREE.WebGLRenderer | null {
    return this.renderer;
  }

  destroy(): void {
    if (this.animationId !== null) {
      cancelAnimationFrame(this.animationId);
      this.animationId = null;
    }

    const el = this.container?.nativeElement;
    if (this.renderer && el) {
      const canvas = this.renderer.domElement;
      if (el.contains(canvas)) {
        el.removeChild(canvas);
      }
      this.renderer.dispose();
    }

    this.scene = null;
    this.camera = null;
    this.renderer = null;
    this.objects.clear();
    this.interactiveObjects = [];
    this.particles = null;
  }
}
