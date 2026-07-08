USE hikebuddy;

-- Sample hike routes: 7 EASY, 7 MEDIUM, 6 HARD
-- Run this after schema.sql to populate sample data
-- Safe to re-run: INSERT IGNORE skips duplicates

INSERT IGNORE INTO HikeRoute (name, region, difficulty, distance, description) VALUES
-- EASY routes
('Turtle Lake Loop', 'Tbilisi', 'EASY', 3.2, 'A gentle loop around the scenic Turtle Lake, perfect for beginners and families.'),
('Botanical Garden Trail', 'Tbilisi', 'EASY', 2.5, 'A peaceful walk through the Tbilisi Botanical Garden with beautiful flora and canyon views.'),
('Kojori Forest Path', 'Kojori', 'EASY', 4.1, 'An easy forest walk near the village of Kojori with views of the surrounding valleys.'),
('Mtskheta Riverside Walk', 'Mtskheta', 'EASY', 3.8, 'A flat riverside trail along the Mtkvari near the ancient capital of Mtskheta.'),
('Sighnaghi Vineyard Trail', 'Kakheti', 'EASY', 5.0, 'A leisurely walk through the vineyards surrounding the romantic town of Sighnaghi.'),
('Gori Valley Stroll', 'Shida Kartli', 'EASY', 4.5, 'A relaxed walk through the Gori Valley with views of the surrounding hills.'),
('Lagodekhi Lake Path', 'Kakheti', 'EASY', 3.0, 'An easy trail to the Black Rock Lake in Lagodekhi protected areas.'),

-- MEDIUM routes
('Kazbegi Gergeti Trail', 'Kazbegi', 'MEDIUM', 9.5, 'A moderately challenging hike to the iconic Gergeti Trinity Church with stunning views of Mount Kazbek.'),
('Borjomi Forest Circuit', 'Samtskhe-Javakheti', 'MEDIUM', 8.2, 'A beautiful circuit through the Borjomi-Kharagauli National Park with mineral spring stops.'),
('Shuakhevi Waterfall Hike', 'Adjara', 'MEDIUM', 7.8, 'A rewarding hike through dense forest to the spectacular Shuakhevi waterfall.'),
('Abudelauri Lakes Trail', 'Kazbegi', 'MEDIUM', 12.0, 'A scenic trail to the three colorful Abudelauri lakes in the Greater Caucasus.'),
('Prometheus Cave Circuit', 'Imereti', 'MEDIUM', 6.5, 'A hike combining the Prometheus Cave visit with a canyon walk along the Kvirila River.'),
('Tusheti Road Trek', 'Tusheti', 'MEDIUM', 10.0, 'A challenging but accessible trek through the remote and beautiful Tusheti region.'),
('Kvareli Alazani Trail', 'Kakheti', 'MEDIUM', 7.2, 'A medium trail along the Alazani River with views of the Caucasus foothills.'),

-- HARD routes
('Mount Kazbek Summit', 'Kazbegi', 'HARD', 28.0, 'A serious mountaineering challenge to the 5047m summit of Mount Kazbek. Requires experience and equipment.'),
('Svaneti Traverse', 'Svaneti', 'HARD', 45.0, 'A multi-day traverse through the legendary Svaneti region with stunning glacier and tower village views.'),
('Tusheti to Khevsureti Cross', 'Tusheti', 'HARD', 35.0, 'A demanding multi-day route crossing from Tusheti to Khevsureti over high mountain passes.'),
('Chaukhi Pass Trek', 'Kazbegi', 'HARD', 18.5, 'A strenuous trek to the dramatic Chaukhi rock formations and the high-altitude pass.'),
('Ushguli Circuit', 'Svaneti', 'HARD', 22.0, 'A tough circuit around the highest permanently inhabited village in Europe with glacier crossings.'),
('Lomisi Ridge Traverse', 'Mtiuleti', 'HARD', 16.0, 'A demanding ridge walk with exposed sections and panoramic views of the central Caucasus.');