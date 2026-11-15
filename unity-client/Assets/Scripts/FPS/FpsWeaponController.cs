using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

namespace DoraMCU.Client.FPS
{
    /// <summary>
    /// Defines a weapon with damage, fire rate, reload time, and ammo.
    /// </summary>
    [System.Serializable]
    public class FpsWeaponDefinition
    {
        public string name;
        public int damage;
        public float fireRate;       // shots per second
        public float reloadTime;     // seconds
        public int magSize;
        public int reserveAmmo;
        public bool hitScan;         // true = instant raycast, false = projectile
        public float projectileSpeed;
        public float recoilPitch;
        public float recoilYaw;
        public float range;
        public GameObject weaponModel;
        public GameObject muzzleFlash;
        public AudioClip[] fireSounds;
        public AudioClip reloadSound;
    }

    /// <summary>
    /// Weapon controller for switching, aiming, and shooting.
    /// Supports both hit-scan (raycast) and projectile weapons.
    /// </summary>
    public class FpsWeaponController : MonoBehaviour
    {
        [Header("Weapon Settings")]
        [SerializeField] private List<FpsWeaponDefinition> weapons = new();
        [SerializeField] private Transform weaponMount;
        [SerializeField] private Transform firePoint;
        [SerializeField] private LayerMask shootableLayers;

        [Header("Aim Settings")]
        [SerializeField] private float aimDownSightOffset = 0.1f;
        [SerializeField] private float aimSensitivityMultiplier = 0.7f;

        [Header("Effects")]
        [SerializeField] private ParticleSystem hitEffect;
        [SerializeField] private GameObject bulletHolePrefab;

        private int currentWeaponIndex;
        private FpsWeaponDefinition currentWeapon;
        private int currentAmmo;
        private int currentReserveAmmo;
        private float fireCooldown;
        private bool isReloading;
        private bool isAiming;
        private Coroutine reloadCoroutine;

        private Vector3 weaponDefaultPosition;
        private Quaternion weaponDefaultRotation;

        // Events
        public event Action<int, int> OnAmmoChanged;
        public event Action<string> OnWeaponChanged;
        public event Action<bool> OnReloadStarted;
        public event Action OnReloadFinished;
        public event Action<bool> OnAimChanged;

        public int CurrentAmmo => currentAmmo;
        public int CurrentReserveAmmo => currentReserveAmmo;
        public int CurrentWeaponIndex => currentWeaponIndex;
        public FpsWeaponDefinition CurrentWeapon => currentWeapon;
        public bool IsReloading => isReloading;
        public bool IsAiming => isAiming;

        void Awake()
        {
            if (weapons.Count > 0)
            {
                EquipWeapon(0);
            }
        }

        void Start()
        {
            if (weaponMount != null)
            {
                weaponDefaultPosition = weaponMount.localPosition;
                weaponDefaultRotation = weaponMount.localRotation;
            }
        }

        void Update()
        {
            HandleWeaponInput();
            HandleAiming();
            UpdateFireCooldown();
        }

        private void HandleWeaponInput()
        {
            // Fire weapon
            if (Input.GetButton("Fire1") && !isReloading && fireCooldown <= 0f)
            {
                Fire();
            }

            // Reload
            if (Input.GetKeyDown(KeyCode.R))
            {
                Reload();
            }

            // Aim down sights
            if (Input.GetButtonDown("Fire2"))
            {
                isAiming = true;
                OnAimChanged?.Invoke(true);
            }
            if (Input.GetButtonUp("Fire2"))
            {
                isAiming = false;
                OnAimChanged?.Invoke(false);
            }

            // Weapon switching with number keys
            if (Input.GetKeyDown(KeyCode.Alpha1) && weapons.Count > 0) EquipWeapon(0);
            if (Input.GetKeyDown(KeyCode.Alpha2) && weapons.Count > 1) EquipWeapon(1);
            if (Input.GetKeyDown(KeyCode.Alpha3) && weapons.Count > 2) EquipWeapon(2);
            if (Input.GetKeyDown(KeyCode.Alpha4) && weapons.Count > 3) EquipWeapon(3);
            if (Input.GetKeyDown(KeyCode.Alpha5) && weapons.Count > 4) EquipWeapon(4);

            // Weapon switching with scroll wheel
            int scroll = (int)Input.GetAxis("Mouse ScrollWheel");
            if (scroll != 0 && weapons.Count > 1)
            {
                currentWeaponIndex = (currentWeaponIndex + (scroll > 0 ? 1 : -1) + weapons.Count) % weapons.Count;
                EquipWeapon(currentWeaponIndex);
            }
        }

        private void HandleAiming()
        {
            if (weaponMount == null) return;

            Vector3 targetPosition = weaponDefaultPosition;
            Quaternion targetRotation = weaponDefaultRotation;

            if (isAiming)
            {
                targetPosition += transform.forward * aimDownSightOffset;
            }

            weaponMount.localPosition = Vector3.Lerp(weaponMount.localPosition, targetPosition, 10f * Time.deltaTime);
            weaponMount.localRotation = Quaternion.Slerp(weaponMount.localRotation, targetRotation, 10f * Time.deltaTime);
        }

        private void UpdateFireCooldown()
        {
            if (fireCooldown > 0f)
            {
                fireCooldown -= Time.deltaTime;
            }
        }

        private void EquipWeapon(int index)
        {
            if (index < 0 || index >= weapons.Count) return;

            currentWeaponIndex = index;
            currentWeapon = weapons[index];
            currentAmmo = currentWeapon.magSize;
            currentReserveAmmo = currentWeapon.reserveAmmo;
            isReloading = false;
            fireCooldown = 0f;

            if (reloadCoroutine != null)
            {
                StopCoroutine(reloadCoroutine);
                reloadCoroutine = null;
            }

            // Hide all weapon models
            foreach (var weapon in weapons)
            {
                if (weapon.weaponModel != null)
                    weapon.weaponModel.SetActive(false);
            }

            // Show current weapon model
            if (currentWeapon.weaponModel != null)
                currentWeapon.weaponModel.SetActive(true);

            OnWeaponChanged?.Invoke(currentWeapon.name);
            OnAmmoChanged?.Invoke(currentAmmo, currentReserveAmmo);
        }

        private void Fire()
        {
            if (currentAmmo <= 0)
            {
                Reload();
                return;
            }

            currentAmmo--;
            OnAmmoChanged?.Invoke(currentAmmo, currentReserveAmmo);

            fireCooldown = 1f / currentWeapon.fireRate;

            // Play fire sound
            if (currentWeapon.fireSounds != null && currentWeapon.fireSounds.Length > 0)
            {
                int soundIndex = UnityEngine.Random.Range(0, currentWeapon.fireSounds.Length);
                AudioSource.PlayClipAtPoint(currentWeapon.fireSounds[soundIndex], transform.position);
            }

            // Apply recoil
            ApplyRecoil();

            if (currentWeapon.hitScan)
            {
                FireHitScan();
            }
            else
            {
                FireProjectile();
            }

            // Show muzzle flash
            if (currentWeapon.muzzleFlash != null)
            {
                currentWeapon.muzzleFlash.SetActive(true);
                Invoke(nameof(HideMuzzleFlash), 0.05f);
            }
        }

        private void FireHitScan()
        {
            FpsPlayerController playerController = GetComponentInParent<FpsPlayerController>();
            if (playerController == null) return;

            Vector3 aimOrigin = playerController.GetCameraPosition();
            Vector3 aimDirection = playerController.GetAimDirection();

            // Add slight spread
            aimDirection += new Vector3(
                UnityEngine.Random.Range(-0.01f, 0.01f),
                UnityEngine.Random.Range(-0.01f, 0.01f),
                0
            ).normalized;

            RaycastHit hit;
            if (Physics.Raycast(aimOrigin, aimDirection, out hit, currentWeapon.range, shootableLayers))
            {
                // Send hit to server for validation
                FpsNetworkManager.Instance?.SendHit(
                    hit.point,
                    hit.transform.gameObject.GetComponent<FpsNetworkPlayer>()?.PlayerId
                );

                // Show hit effect
                if (hitEffect != null)
                {
                    ParticleSystem instance = Instantiate(hitEffect, hit.point, Quaternion.identity);
                    Destroy(instance.gameObject, 1f);
                }

                // Show bullet hole
                if (bulletHolePrefab != null && hit.normal != Vector3.zero)
                {
                    Quaternion rotation = Quaternion.FromToRotation(Vector3.up, hit.normal);
                    GameObject hole = Instantiate(bulletHolePrefab, hit.point, rotation);
                    Destroy(hole, 30f);
                }
            }
        }

        private void FireProjectile()
        {
            if (firePoint == null) return;

            // Create projectile
            GameObject projectile = new GameObject("Projectile");
            Rigidbody rb = projectile.AddComponent<Rigidbody>();
            rb.velocity = firePoint.forward * currentWeapon.projectileSpeed;
            rb.useGravity = true;

            // Add collider
            Collider col = projectile.AddComponent<SphereCollider>();
            col.radius = 0.1f;
            col.isTrigger = true;

            // Set position
            projectile.transform.position = firePoint.position;

            // Destroy after range time
            float lifetime = currentWeapon.range / currentWeapon.projectileSpeed;
            Destroy(projectile, lifetime);

            // Send projectile to server
            FpsNetworkManager.Instance?.SendProjectile(firePoint.position, firePoint.forward * currentWeapon.projectileSpeed);
        }

        private void ApplyRecoil()
        {
            FpsPlayerController playerController = GetComponentInParent<FpsPlayerController>();
            if (playerController == null) return;

            // Recoil is applied via network state reconciliation
            // Client-side prediction handles visual recoil
        }

        private void Reload()
        {
            if (isReloading || currentAmmo == currentWeapon.magSize || currentReserveAmmo <= 0)
                return;

            isReloading = true;
            OnReloadStarted?.Invoke(true);

            if (currentWeapon.reloadSound != null)
            {
                AudioSource.PlayClipAtPoint(currentWeapon.reloadSound, transform.position);
            }

            reloadCoroutine = StartCoroutine(ReloadCoroutine());
        }

        private IEnumerator ReloadCoroutine()
        {
            yield return new WaitForSeconds(currentWeapon.reloadTime);

            int needed = currentWeapon.magSize - currentAmmo;
            int available = Mathf.Min(needed, currentReserveAmmo);

            currentAmmo += available;
            currentReserveAmmo -= available;

            isReloading = false;
            OnAmmoChanged?.Invoke(currentAmmo, currentReserveAmmo);
            OnReloadStarted?.Invoke(false);
            OnReloadFinished?.Invoke();

            reloadCoroutine = null;
        }

        private void HideMuzzleFlash()
        {
            if (currentWeapon?.muzzleFlash != null)
                currentWeapon.muzzleFlash.SetActive(false);
        }

        /// <summary>
        /// Update ammo from server state.
        /// </summary>
        public void SetAmmo(int ammo, int reserve)
        {
            currentAmmo = ammo;
            currentReserveAmmo = reserve;
            OnAmmoChanged?.Invoke(currentAmmo, currentReserveAmmo);
        }

        /// <summary>
        /// Add a weapon to the inventory.
        /// </summary>
        public void AddWeapon(FpsWeaponDefinition weapon)
        {
            weapons.Add(weapon);
        }
    }
}
