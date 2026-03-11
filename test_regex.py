import re
text = "بِسْمِ [h:1[ٱ]للَّهِ [h:2[ٱ][l[ل]رَّحْمَ[n[ـٰ]نِ [h:3[ٱ][l[ل]رَّح[p[ِي]مِ"
pattern = re.compile(r"\[([a-z])[^\[]*\[([^\[\]]+)\]")
print(pattern.findall(text))
