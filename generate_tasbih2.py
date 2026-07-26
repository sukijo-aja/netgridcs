import math

def get_circle_path(cx, cy, r):
    return f"M {cx:.2f},{cy-r:.2f} a {r},{r} 0 1,0 0,{2*r} a {r},{r} 0 1,0 0,{-2*r}"

cx, cy = 13.5, 9.5
R = 7.0
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
# Direction is vector (-0.707, 0.707)
dir_x, dir_y = math.cos(math.radians(135)), math.sin(math.radians(135))
perp_x, perp_y = -dir_y, dir_x # (-0.707, -0.707)

def get_point(dist, perp_offset):
    px = cx + dir_x * dist + perp_x * perp_offset
    py = cy + dir_y * dist + perp_y * perp_offset
    return (px, py)

def pt(p): return f"{p[0]:.2f},{p[1]:.2f}"

# stem
stem_dist_start = R - 0.5
stem_dist_end = R + 1.5
stem_width = 1.4
p1 = get_point(stem_dist_start, stem_width/2)
p2 = get_point(stem_dist_end, stem_width/2)
p3 = get_point(stem_dist_end, -stem_width/2)
p4 = get_point(stem_dist_start, -stem_width/2)
paths.append(f"M {pt(p1)} L {pt(p2)} L {pt(p3)} L {pt(p4)} Z")

# tassel body (rounded shoulders)
body_start = R + 1.0
body_end = R + 7.5
body_w = 3.6
corner_r = 1.2

# Path with rounded shoulders
# Start bottom right, line to bottom left, line to top left, curve to stem, line to top right, curve down
b_bl = get_point(body_end, -body_w/2)
b_br = get_point(body_end, body_w/2)

b_tr = get_point(body_start + corner_r, body_w/2)
b_tl = get_point(body_start + corner_r, -body_w/2)

c_r = get_point(body_start, body_w/2 - corner_r)
c_l = get_point(body_start, -body_w/2 + corner_r)

# Draw polygon for body just using lines for simplicity, but let's make it look like the image (a bit bulky)
t_p1 = get_point(body_start, body_w/2)
t_p2 = get_point(body_end, body_w/2)
t_p3 = get_point(body_end, -body_w/2)
t_p4 = get_point(body_start, -body_w/2)

# Create a shape with rounded top
paths.append(f"M {pt(t_p1)} L {pt(t_p2)} L {pt(t_p3)} L {pt(t_p4)} " +
             f"Q {pt(get_point(body_start - 1.0, 0))} {pt(t_p1)} Z")

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

with open("app/src/main/res/drawable/ic_tasbih.xml", "w") as f:
    f.write(xml)
