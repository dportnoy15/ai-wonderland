from py4j.java_gateway import JavaGateway, CallbackServerParameters

if __name__ == "__main__":
    gateway = JavaGateway(
        callback_server_parameters=CallbackServerParameters())
    #listener = PythonListener(gateway)
    #gateway.entry_point.registerListener(listener)
    #gateway.entry_point.notifyAllListeners()

    stack = gateway.entry_point.getStack()

    stack.push("First %s" % ('item'))
    stack.push("Second item")
    print(stack.pop())
    print(stack.pop())

    gateway.entry_point.setValue(1337)

    gateway.close()