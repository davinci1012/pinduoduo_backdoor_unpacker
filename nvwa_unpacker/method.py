from pwn import *
context.endian = 'big'


class Method:
    def __init__(self):
        self.stack_size = 0
        self.local_size = 0
        pass

    def parse(self, parent):
        buf = parent.buf
        flag = u8(buf.get(1))
        self.flag = flag
        clz_idx = u32(buf.get(4))
        str1_idx = u32(buf.get(4))
        str2_idx = u32(buf.get(4))
        str3_idx = u32(buf.get(4))
        self.clz = parent.class_pool[clz_idx]
        self.name = parent.string_pool[str1_idx]
        self.signature = parent.string_pool[str2_idx]
        self.wtf_str = parent.string_pool[str3_idx]
        eaten = 17
        # print("%s.%s signature = %s wtf = %s"%(parent.class_pool[clz_idx].classname,parent.string_pool[str1_idx],parent.string_pool[str2_idx],parent.string_pool[str3_idx]))
        if (flag & 4) != 0:
            nop = u32(buf.get(4))
            buf.get(nop)
            eaten += 4 + nop
        if (flag & 2) == 0:
            return eaten
        val1 = u16(buf.get(2))
        self.stack_size = u32(buf.get(4))
        self.local_size = u32(buf.get(4))
        self.method_id = u32(buf.get(4))
        val5 = u16(buf.get(2))
        eaten += 16
        self.exception_table = []
        for i in range(val5):
            self.exception_table.append([
                u32(buf.get(4)),
                u32(buf.get(4)),
                u32(buf.get(4)),
                u32(buf.get(4)),
            ])
            eaten += 16
        code_size = u32(buf.get(4))
        self.code = buf.get(code_size)
        eaten += 4 + code_size
        # print("code size: %#x"%code_size)
        if (flag & 8) != 0:
            nop = u32(buf.get(4))
            buf.get(nop)
            eaten += 4 + nop
        return eaten
