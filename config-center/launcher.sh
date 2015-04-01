#定义程序启动变量, 根据实际情况修改
APP_MAIN_CLASS="com.moreopen.config.center.ConfigCenterLauncher"
APP_JMX_PORT="3344"
MMS=512m
MMX=512m
#指定 java 启动参数, 以空格分隔
ARGS=""

#检查应用是否已启动，如果已启动则自动杀掉对应进程
PID=`ps -ef | grep $APP_MAIN_CLASS | grep -v ' grep' | awk '{print $2}'`
if [ ! -e $PID ];
then
	echo "killing $APP_MAIN_CLASS, PID is $PID"
	kill -9 $PID
	sleep 2
fi

#JMX_OPTS="-Dcom.sun.management.jmxremote.port=$APP_JMX_PORT -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"

JAVA_OPTS="-server -Xms$MMS -Xmx$MMX -XX:PermSize=64m -XX:MaxPermSize=128m"
JAVA_OPTS="$JAVA_OPTS -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:ParallelGCThreads=8"
JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:gc.log"
#开启远程调试，默认关闭
#JAVA_OPTS="$JAVA_OPTS -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n"

CURRENT_DIR=`pwd`
#设置 classpath 指向的路径, 可设置相对路径，也可设置绝对路径, exp: #CLASSPATH="$CURRENT_DIR/bin:$CURRENT_DIR/conf:$CURRENT_DIR/classes:$CURRENT_DIR/lib/*"
CLASSPATH="$CURRENT_DIR/bin:$CURRENT_DIR/conf:$CURRENT_DIR/classes:$CURRENT_DIR/lib/*"

#指定系统日志和错误日志输出的日志文件
LOG_PATH="std_out.log"

java $JAVA_OPTS $JMX_OPTS -cp $CLASSPATH $APP_MAIN_CLASS $ARGS >> $LOG_PATH 2>&1 &

echo "Start $APP_MAIN_CLASS, done."
