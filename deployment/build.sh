ROOT_DIR="$(cd ..; pwd)"

# Build File Manager
cd ${ROOT_DIR}/filemgr
mvn clean install -nsu -DskipTests
docker build . -t oodt/filemgr

# Build Tomcat
cd ${ROOT_DIR}
mvn clean install -nsu -DskipTests -pl webapp/fmprod
cd ${ROOT_DIR}/webapp
docker build . -t oodt/tomcat

# Build OPSUI
cd ${ROOT_DIR}/react-components/oodt_opsui_sample_app
docker build . -t oodt/opsui

cd ${ROOT_DIR}/deployment
echo "Done!"