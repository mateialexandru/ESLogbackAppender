DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
java -XX:MaxPermSize=250M -Xmx800m -jar $DIR/sbt-launch-0.12.2.jar $*