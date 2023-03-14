from pwn import *
context.endian = 'big'


class JavaClass:
    def __init__(self, idx):
        self.idx = idx
        self.is_interface = False
        self.static_fields = []
        self.fields = []
        self.interface_class = []
        self.parent_class = []  # always 0
        self.static_method = []
        self.method = []
    def is_public(self):
        return (self.wtf_flag & 1) == 1

    def parse(self, parent):
        buf = parent.buf
        flag = u8(buf.get(1))
        self.flag = flag
        str_idx = u32(buf.get(4))
        eaten = 5
        self.is_vmp = (flag & 2) != 0
        self.name = parent.string_pool[str_idx]
        # print("parse class: %s flag: %#x"%(self.classname,flag))
        if (flag & 4) != 0:
            nop_size = u32(buf.get(4))
            buf.get(nop_size)
            eaten += 4 + nop_size
        if self.is_vmp == False:
            return eaten
        tmp_idx = u32(buf.get(4))
        self.parent = parent.class_pool[tmp_idx]
        self.wtf_flag = u16(buf.get(2))
        interface_size = u16(buf.get(2))
        eaten += 8
        for i in range(interface_size):
            tmp_idx = u32(buf.get(4))
            eaten += 4
            clz = parent.class_pool[tmp_idx]
            clz.is_interface = True
            self.interface_class.append(clz)

        # always zero for self.parent_class
        class_list_size = u16(buf.get(2))
        eaten += 2
        for i in range(class_list_size):
            tmp_idx = u32(buf.get(4))
            eaten += 4
            self.parent_class.append(parent.class_pool[tmp_idx])

        ptr_size = u16(buf.get(2))
        eaten += 2

        for i in range(ptr_size):
            tmp_idx = u32(buf.get(4))
            eaten += 4
            self.static_fields.append(tmp_idx)

        ptr_size = u16(buf.get(2))
        eaten += 2

        for i in range(ptr_size):
            tmp_idx = u32(buf.get(4))
            eaten += 4
            self.fields.append(tmp_idx)

        ptr_size = u16(buf.get(2))
        eaten += 2

        for i in range(ptr_size):
            tmp_idx = u32(buf.get(4))
            eaten += 4
            self.static_method.append(tmp_idx)

        ptr_size = u16(buf.get(2))
        eaten += 2

        for i in range(ptr_size):
            tmp_idx = u32(buf.get(4))
            eaten += 4
            self.method.append(tmp_idx)
        return eaten
