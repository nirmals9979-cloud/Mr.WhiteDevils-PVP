These stubs were written by me from public 26.x sources. They only prove the adapter is internally consistent Java.
They do NOT prove the real Minecraft 26.3 / Fabric API signatures match. Real check = `gradle build`.
Run: java -m jdk.compiler/com.sun.tools.javac.Main -d /tmp/o $(find tools/stubcheck -name '*.java') $(find src/main -name '*.java')
