-- V102: Seed 3D positions for Earth-based AI admin characters
-- Updates galaxy_name to "Milky Way" and assigns 3D coordinates based on real-world capital city positions
-- Earth is positioned at galaxy center (0, 0, 0) with capital cities on a sphere of radius ~10 units
-- Formula: x = r * cos(lat) * sin(lon), y = r * sin(lat), z = r * cos(lat) * cos(lon)
--
-- AI character IDs from V34:
-- Country leaders: US=2, CN=3, RU=4, GB=5, DE=6, FR=7, JP=8, IN=9, BR=10, CA=11,
--                  AU=12, KR=13, MX=14, IT=15, ES=16, NG=17, SA=18, TR=19, AR=20, ZA=21
-- Region governors: CA=22, TX=23, NY=24, FL=25, Bavaria=26, Berlin=27,
--                   Tokyo=28, Osaka=29, Guangdong=30, Shanghai=31, Maharashtra=32, Karnataka=33
-- UN Secretary-General: ID=34

-- ============================================================
-- Country Leaders (20) — positioned at their capital cities
-- ============================================================

-- ID 2: Joe Biden, President of the United States — Washington D.C. (38.9072°N, 77.0369°W)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = 6.85, pos_y = 6.43, pos_z = -5.29
WHERE id = 2;

-- ID 3: Xi Jinping, President of China — Beijing (39.9042°N, 116.4074°E)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = 3.60, pos_y = 6.44, pos_z = 7.03
WHERE id = 3;

-- ID 4: Vladimir Putin, President of Russia — Moscow (55.7558°N, 37.6173°E)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = 6.47, pos_y = 8.30, pos_z = -1.93
WHERE id = 4;

-- ID 5: Rishi Sunak, Prime Minister of the United Kingdom — London (51.5074°N, 0.1278°W)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = 0.02, pos_y = 7.90, pos_z = -6.57
WHERE id = 5;

-- ID 6: Olaf Scholz, Chancellor of Germany — Berlin (52.5200°N, 13.4050°E)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = 2.30, pos_y = 8.03, pos_z = -5.83
WHERE id = 6;

-- ID 7: Emmanuel Macron, President of France — Paris (48.8566°N, 2.3522°E)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = 0.41, pos_y = 7.56, pos_z = -6.28
WHERE id = 7;

-- ID 8: Fumio Kishida, Prime Minister of Japan — Tokyo (35.6762°N, 139.6503°E)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = 5.93, pos_y = 5.84, pos_z = 5.57
WHERE id = 8;

-- ID 9: Narendra Modi, Prime Minister of India — New Delhi (28.6139°N, 77.2090°E)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = 4.57, pos_y = 4.79, pos_z = 7.73
WHERE id = 9;

-- ID 10: Luiz Inacio Lula da Silva, President of Brazil — Brasília (15.7975°S, 47.8919°W)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = -7.27, pos_y = -2.73, pos_z = -5.33
WHERE id = 10;

-- ID 11: Justin Trudeau, Prime Minister of Canada — Ottawa (45.4215°N, 75.6972°W)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = 6.53, pos_y = 7.13, pos_z = -5.93
WHERE id = 11;

-- ID 12: Anthony Albanese, Prime Minister of Australia — Canberra (35.2809°S, 149.1300°E)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = 6.37, pos_y = -5.78, pos_z = 4.83
WHERE id = 12;

-- ID 13: Yoon Suk Yeol, President of South Korea — Seoul (37.5665°N, 126.9780°E)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = 5.23, pos_y = 6.10, pos_z = 6.17
WHERE id = 13;

-- ID 14: Claudia Sheinbaum, President of Mexico — Mexico City (19.4326°N, 99.1332°W)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = -8.63, pos_y = 3.33, pos_z = -3.53
WHERE id = 14;

-- ID 15: Giorgia Meloni, Prime Minister of Italy — Rome (41.9028°N, 12.4964°E)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = 2.17, pos_y = 6.69, pos_z = -6.36
WHERE id = 15;

-- ID 16: Pedro Sanchez, Prime Minister of Spain — Madrid (40.4168°N, 3.7038°W)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = -0.65, pos_y = 6.50, pos_z = -6.39
WHERE id = 16;

-- ID 17: Bola Tinubu, President of Nigeria — Abuja (9.0765°N, 7.3986°E)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = 1.29, pos_y = 1.58, pos_z = -9.87
WHERE id = 17;

-- ID 18: Salman bin Abdulaziz, King of Saudi Arabia — Riyadh (24.7136°N, 46.6753°E)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = 8.13, pos_y = 4.20, pos_z = -4.87
WHERE id = 18;

-- ID 19: Recep Tayyip Erdogan, President of Turkey — Ankara (39.9334°N, 32.8597°E)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = 5.73, pos_y = 6.45, pos_z = -5.63
WHERE id = 19;

-- ID 20: Javier Milei, President of Argentina — Buenos Aires (34.6037°S, 58.3816°W)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = -8.87, pos_y = -5.68, pos_z = -3.13
WHERE id = 20;

-- ID 21: Cyril Ramaphosa, President of South Africa — Pretoria (25.7479°S, 28.1894°E)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = 4.85, pos_y = -4.35, pos_z = -7.23
WHERE id = 21;

-- ============================================================
-- Region Governors (12) — positioned near their regional capitals
-- Offset by 0.5 units from capital to avoid overlap with country leaders
-- ============================================================

-- ID 22: Gavin Newsom, Governor of California — Sacramento (38.5816°N, 121.4944°W)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = -9.35, pos_y = 6.25, pos_z = -3.45
WHERE id = 22;

-- ID 23: Greg Abbott, Governor of Texas — Austin (30.2672°N, 97.7431°W)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = -8.47, pos_y = 5.04, pos_z = -4.63
WHERE id = 23;

-- ID 24: Kathy Hochul, Governor of New York — Albany (42.6526°N, 73.7562°W)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = 6.23, pos_y = 6.78, pos_z = -5.73
WHERE id = 24;

-- ID 25: Ron DeSantis, Governor of Florida — Tallahassee (30.4383°N, 84.2807°W)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = -7.33, pos_y = 5.07, pos_z = -5.37
WHERE id = 25;

-- ID 26: Markus Soder, Minister-President of Bavaria — Munich (48.1351°N, 11.5820°E)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = 2.02, pos_y = 7.46, pos_z = -6.33
WHERE id = 26;

-- ID 27: Kai Wegner, Regierender Burgermeister of Berlin — Berlin (52.5200°N, 13.4050°E)
-- Offset slightly from Chancellor's position to avoid overlap
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = 2.80, pos_y = 8.10, pos_z = -5.70
WHERE id = 27;

-- ID 28: Yuriko Koike, Governor of Tokyo — Tokyo (35.6762°N, 139.6503°E)
-- Offset slightly from PM's position to avoid overlap
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = 6.43, pos_y = 5.90, pos_z = 5.45
WHERE id = 28;

-- ID 29: Ichiro Matsui, Governor of Osaka — Osaka (34.6937°N, 135.5023°E)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = 5.53, pos_y = 5.59, pos_z = 5.77
WHERE id = 29;

-- ID 30: Huang Kunhu, Governor of Guangdong — Guangzhou (23.1291°N, 113.2644°E)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = 3.27, pos_y = 3.93, pos_z = 7.87
WHERE id = 30;

-- ID 31: Gong Zheng, Party Secretary of Shanghai — Shanghai (31.2304°N, 121.4737°E)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = 5.37, pos_y = 5.18, pos_z = 6.07
WHERE id = 31;

-- ID 32: Bhagat Singh Koshyari, Governor of Maharashtra — Mumbai (19.0760°N, 72.8777°E)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = 4.23, pos_y = 3.27, pos_z = 8.17
WHERE id = 32;

-- ID 33: Vajubhai Vala, Governor of Karnataka — Bengaluru (12.9716°N, 77.5946°E)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = 4.63, pos_y = 2.24, pos_z = 7.97
WHERE id = 33;

-- ============================================================
-- UN Secretary-General (1) — positioned at Earth center
-- ============================================================

-- ID 34: UN Secretary-General AI — Earth center (0, 0, 0)
UPDATE ai_characters SET
  galaxy_name = 'Milky Way',
  pos_x = 0.0, pos_y = 0.0, pos_z = 0.0
WHERE id = 34;
