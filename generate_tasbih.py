import math

def get_circle_path(cx, cy, r):
    # generate SVG path for a circle
    return f"M {cx},{cy-r} A {r},{r} 0 1,0 {cx},{cy+r} A {r},{r} 0 1,0 {cx},{cy-r}"

cx, cy = 14, 10
R = 7.2
r = 1.6
n_spots = 16
paths = []

for k in range(n_spots):
    angle_deg = k * 360 / n_spots
    if angle_deg == 135:
        continue # skip the spot for tassel
    
    angle_rad = math.radians(angle_deg)
    bx = cx + R * math.cos(angle_rad)
    by = cy + R * math.sin(angle_rad)
    paths.append(get_circle_path(bx, by, r))

# Tassel at 135 degrees
# Direction is vector (cos(135), sin(135)) = (-0.707, 0.707)
# Let's define the tassel shape.
# Stem from circle radius R-0.5 to R+2.5
# Tassel body from R+2.5 to R+8
dir_x, dir_y = math.cos(math.radians(135)), math.sin(math.radians(135))
perp_x, perp_y = -dir_y, dir_x # perpendicular vector (-0.707, -0.707)

def get_point(dist, perp_offset):
    px = cx + dir_x * dist + perp_x * perp_offset
    py = cy + dir_y * dist + perp_y * perp_offset
    return f"{px:.2f},{py:.2f}"

# stem
stem_dist_start = R - 0.5
stem_dist_end = R + 2.5
stem_width = 1.2
p1 = get_point(stem_dist_start, stem_width/2)
p2 = get_point(stem_dist_end, stem_width/2)
p3 = get_point(stem_dist_end, -stem_width/2)
p4 = get_point(stem_dist_start, -stem_width/2)
paths.append(f"M {p1} L {p2} L {p3} L {p4} Z")

# tassel body
body_dist_start = R + 2.5
body_dist_end = R + 7.5
body_width = 3.5
b1 = get_point(body_dist_start, body_width/2)
b2 = get_point(body_dist_end, body_width/2)
b3 = get_point(body_dist_end, -body_width/2)
b4 = get_point(body_dist_start, -body_width/2)

# Make it a rounded rect or just a polygon
paths.append(f"M {b1} L {b2} L {b3} L {b4} Z")

xml = f"""<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
  <path
      android:fillColor="@android:color/black"
      android:pathData="{' '.join(paths)}" />
</vector>
"""

with open("ic_tasbih.xml", "w") as f:
    f.write(xml)

