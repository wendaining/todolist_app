# Linux/WSL 开发环境清单（Java 主线）

## 1. 当前已安装结果（2026-03-31）

- JDK: 21.0.8（LTS）
- Maven: 3.9.14
- Node/npm: 已可用（用于 web）

安装路径：

- JAVA_HOME: /home/wendaining/.jdk/jdk-21.0.8
- MAVEN_HOME: /home/wendaining/.maven/maven-3.9.14

## 2. 已写入的 zsh 配置

已在 ~/.zshrc 增加：

1. export JAVA_HOME="$HOME/.jdk/jdk-21.0.8"
2. export MAVEN_HOME="$HOME/.maven/maven-3.9.14"
3. export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH"

## 3. 重新加载配置

```bash
source ~/.zshrc
```

## 4. 验证命令

```bash
java -version
javac -version
mvn -version
npm -v
```

## 5. 常见问题

1. 只有 java 没有 javac：说明只有 JRE，缺少 JDK。
2. mvn 找不到：检查 MAVEN_HOME 和 PATH 是否生效。
3. 新终端版本不对：执行 `source ~/.zshrc` 后重试。
