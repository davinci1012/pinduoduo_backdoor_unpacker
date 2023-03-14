#!/usr/bin/env python3
# -*- coding:utf-8 -*-
from pwn import *
from JavaDex import JavaDex
from Disasm import Disasm
import zlib
context.endian = 'big'
if len(sys.argv) != 3:
    print("./run.py <nw0.bin> <output dir>")
    exit(1)
with open(sys.argv[1],"rb") as f:
    nw0 = f.read()
nw1 = sys.argv[2]
os.makedirs(nw1,exist_ok = True)
print("input file: %s, output dir: %s"%(sys.argv[1],sys.argv[2]))
buf = Buffer()
buf.add(nw0)
header = u32(buf.get(4),endian='little')
if header != 0xDAC09FAB:
    print("header not correct")
    exit(0)
buf.get(4)
version = u32(buf.get(4))
if (version >> 16) != 1 or (version & 0xff00) != 0:
    print("version not correct")
    exit(0)
xorkey = [0xEC, 0x9A, 0x75, 0xBF, 0xEC, 0x2E, 0xA4, 0xDC, 0x51,0xBF, 0x5A, 0x72, 0xEA, 0xAD, 0x64, 0xD3]
for i in range(0x10):
    d = (version >> (24 - 8*(i%4)))&0xff
    xorkey[i] = xorkey[i] ^d
keys = [u32(bytes(xorkey[0:4])), u32(bytes(xorkey[4:8])),u32(bytes(xorkey[8:12])),u32(bytes(xorkey[12:]))]

print("use xorkey = ",list(map(hex,keys)))
plain = b""
while len(buf) > 0:
    v1 = u32(buf.get(4))
    v2 = u32(buf.get(4))
    s = 0xB5779B70
    for i in range(16):
        v2 -= (keys[2] + 16 * v1) ^ (v1 + s) ^ (keys[3] + (v1>>5))
        v2 &= 0xffffffff
        v1 -= (keys[0] + 16 * v2) ^ (s + v2) ^ (keys[1] + (v2>>5))
        v1 &= 0xffffffff
        s += 0x64A88649
    plain += p32(v1) + p32(v2)

data = zlib.decompress(plain[4:])
buf = Buffer()
buf.add(data)
dex = JavaDex(buf,nw1 + "/fake.dex")
dex.parse()
os.system("cd %s;~/dex-tools-2.1/d2j-dex2jar.sh fake.dex;mkdir -p fake;cd fake;unzip -o ../fake-dex2jar.jar;rm ../fake-dex2jar.jar ../fake.dex"%nw1)
d = Disasm()
for idx in range(len(dex.class_pool)):
    clz = dex.class_pool[idx]
    if not clz.is_vmp:
        continue
    out = d.generate_class(dex,clz)
    if "unknown_asm"in out:
        print("parse %s fail"%clz.name)
        print(out)
        break
    name = clz.name
    print("parse %s"%name)
    dirname = name[:name.rfind("/")]
    os.makedirs(nw1 + "/" + dirname, exist_ok=True)
    target_j = "%s/%s.j"%(nw1,name)
    target_class = "%s/jar/%s.class"%(nw1,name)
    with open(target_j,"w+") as f:
        f.write(out)
    # thanks to https://github.com/Storyyeller/Krakatau/tree/v2
    cmd = "krak2 asm --out '%s' '%s'"%(target_class,target_j)
    result = subprocess.run(["bash","-c",cmd],stdout=subprocess.PIPE,stderr=subprocess.PIPE,encoding='utf8')
    if result.returncode != 0:
        print("compile %s fail"%name)
        print(result.stdout,result.stderr)




