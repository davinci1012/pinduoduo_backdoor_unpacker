
from pwn import *
class Field:
    def __init__(self):
        self.idx = None
        self.raw = None

    def parse(self,parent):
        buf = parent.buf
        flag = u8(buf.get(1))
        self.class_idx = u32(buf.get(4))
        self.name_idx = u32(buf.get(4))
        self.type_idx = u32(buf.get(4))
        self.clz = parent.class_pool[self.class_idx]
        self.name = parent.string_pool[self.name_idx]
        self.type = parent.string_pool[self.type_idx]
        # print("field %s %s %s"%(clz.classname,name,typ))
        eaten = 13
        self.flag = flag
        if (flag & 4) != 0:
            nop = u32(buf.get(4))
            buf.get(nop)
            eaten += 4 + nop
        if (flag & 2) != 0:
            self.raw = u16(buf.get(2))
            #print("%s field %s flag = %#x type = %s raw = %#x"%(self.clz.name,self.name,flag,self.type,self.raw))
            eaten += 2
            if (flag) & 8 != 0:
                nop = u32(buf.get(4))
                buf.get(nop)
                eaten += nop + 4
        return eaten

    def is_public(self):
        return "public"
        if self.raw == None:
            return "public"
        flag = self.raw & 3
        if flag == 1:
            return "private"
        if flag == 2:
            return "public"
        if flag == 3:
            return "protected"
        return "public"


