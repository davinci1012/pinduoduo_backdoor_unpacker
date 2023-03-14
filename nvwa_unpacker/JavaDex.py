from pwn import *
from javaClass import JavaClass
from field import Field
from method import Method
context.endian = 'big'

class JavaDex:
    def __init__(self, buf,dex_target):
        self.string_pool = []
        self.constant_pool = []
        self.class_pool = []
        self.field_pool = []
        self.method_pool = []
        self.buf = buf
        self.dex_target = dex_target
    def find_class(self,clzname):
        for i in self.class_pool:
            if i.name == clzname:
                return i
        return None

    def engine_handler(self, size):
        flag1 = u16(self.buf.get(2))
        flag2 = u16(self.buf.get(2))
        flag3 = u16(self.buf.get(2))
        print("engine flag: %#x %#x %#x" % (flag1, flag2, flag3))
        return 6

    def string_handler(self, size):
        string_size = u32(self.buf.get(4))
        idx = 0
        eaten = 4
        print("string size: %#x total: %#x" % (string_size, size))
        while idx < string_size:
            str_len = u16(self.buf.get(2))
            raw = self.buf.get(str_len).decode("utf8")
            self.string_pool.append(raw)
            eaten += 2 + str_len
            idx += 1
        return eaten

    def constant_handler(self, size):
        cons_size = u32(self.buf.get(4))
        idx = 0
        eaten = 4
        print("constant size: %#x total: %#x" % (cons_size, size))
        while idx < cons_size:
            flag = u8(self.buf.get(1))
            dword = u32(self.buf.get(4))
            self.constant_pool.append([flag,dword])
            eaten += 5
            idx += 1
        return eaten

    def class_handler(self, size):
        class_size = u32(self.buf.get(4))
        eaten = 4
        idx = 0
        self.class_pool = [JavaClass(i) for i in range(class_size)]
        while idx < class_size:
            tmp = self.class_pool[idx].parse(self)
            eaten += tmp
            idx += 1
        return eaten

    def field_handler(self, size):
        field_size = u32(self.buf.get(4))
        eaten = 4
        idx = 0
        self.field_pool = [Field() for i in range(field_size)]
        print("parse %#x field" % field_size)
        while idx < field_size:
            tmp = self.field_pool[idx].parse(self)
            eaten += tmp
            idx += 1
        # relink class
        idx = 0
        for i in range(len(self.class_pool)):
            clz = self.class_pool[i]
            for j in range(len(clz.static_fields)):
                clz.static_fields[j] = self.field_pool[clz.static_fields[j]]
                clz.static_fields[j].idx = j
            for j in range(len(clz.fields)):
                clz.fields[j] = self.field_pool[clz.fields[j]]
                clz.fields[j].idx = j
        return eaten

    def method_handler(self, size):
        method_size = u32(self.buf.get(4))
        eaten = 4
        print("parse %#x method" % (method_size))
        self.method_pool = [Method() for i in range(method_size)]
        for i in range(method_size):
            eaten += self.method_pool[i].parse(self)
        for i in range(len(self.class_pool)):
            clz = self.class_pool[i]
            for j in range(len(clz.static_method)):
                clz.static_method[j] = self.method_pool[clz.static_method[j]]
            for j in range(len(clz.method)):
                clz.method[j] = self.method_pool[clz.method[j]]
        return eaten

    def loaddex_handler(self, size):
        dex_size = u32(self.buf.get(4))
        print("dex size: %#x" % dex_size)
        dex = self.buf.get(dex_size + 0x21)
        write(self.dex_target, dex)
        return 4 + dex_size + 0x21

    def handler_op(self, op, length):
        ophander = {
            0: self.engine_handler,
            1: self.string_handler,
            2: self.constant_handler,
            3: self.class_handler,
            4: self.field_handler,
            5: self.method_handler,
            6: self.loaddex_handler
        }
        eaten = ophander[op](length)
        if eaten != length:
            raise Exception(
                "op %#x not correct, should eaten: %#x, real: %#x" % (op, length, eaten))

    def parse(self):
        while len(self.buf) > 0:
            op = u8(self.buf.get(1))
            length = u32(self.buf.get(4), endian='big')
            self.handler_op(op, length)
            print(len(self.buf))

