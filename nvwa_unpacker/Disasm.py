
from pwn import *
from ops import Opcode
from javaClass import JavaClass
context.endian = 'big'

class Disasm:
    def __init__(self):
        pass

    def insload(self, code):
        buf = Buffer()
        buf.add(code)
        ins_size = u32(buf.get(4))
        # print("ins size = %#x" % ins_size)
        ins = [Opcode() for _ in range(ins_size)]
        for i in range(ins_size):
            try:
                ins[i].parse(buf)
            except Exception as e:
                print("parse opcode %d/%d %#x error"%(i,ins_size,ins[i].opcode))
                for idx in range(i):
                    print(ins[idx])
                raise e
        if len(buf) != 0:
            print("asm code error happen: ")
            for i in ins:
                print(str(i))
            raise Exception("parse code not match length %d" % len(buf))
        return ins

    def asm_ins(self, ins, ctx):
        data = ""
        for idx in range(len(ins)):
            i = ins[idx]
            data += "L_%d:      " % idx + i.to_asm(ctx) + "\n"
        return data

    def generate_class(self, dex, clz: JavaClass):
        is_public = "public" if clz.is_public() else ""
        is_interface = "interface abstract" if clz.is_interface else "super"
        with open("class_temp.txt", "r") as f:
            temp = f.read()
        implements = ""
        for imp in clz.interface_class:
            implements += ".implements %s\n"%imp.name
        fields = ""
        if len(clz.static_fields) > 0:
            fields += "\n".join(map(lambda x: ".field %s static %s %s" %
                                (x.is_public(),x.name, x.type), clz.static_fields))
        fields += "\n"
        if len(clz.fields) > 0:
            fields += "\n".join(map(lambda x: ".field %s %s %s" %
                                (x.is_public(),x.name, x.type), clz.fields))
        methods = ""
        for method in clz.method:
            if clz.is_interface:
                stack_declare = ""
            else:
                stack_declare = "    .code stack %d locals %d"%(method.stack_size, method.local_size)
            ins = self.insload(method.code)
            raw = self.asm_ins(ins, dex)
            catch_declare = ""
            for x in method.exception_table:
                if x[3] == 0xffffffff:
                    name = "[0]"
                else:
                    name = dex.class_pool[x[3]].name
                catch_declare += "        .catch %s from L_%d to L_%d using L_%d\n"%(name,x[0],x[1],x[2])
            tmp = """.method public %s %s : %s
%s
%s
%s
    %s
.end method
"""
            methods += tmp % ("abstract" if clz.is_interface else "",  method.name, method.signature,stack_declare,catch_declare, raw,"" if clz.is_interface else ".end code")
        return temp.format(is_public=is_public,
                           is_interface=is_interface,
                           name=clz.name,
                           parent_class=clz.parent.name,
                           implements=implements,
                           fields=fields,
                           methods=methods,
                           )
