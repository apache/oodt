# OODT-docker

Automatic RADIX build for Apache OODT

## Usage

You should edit docker file replacing the parameters below with whatever you want before build the image.

1. The groupId is a place to specify your company's namespace. 
    ```dockerfile
    -DgroupId = com.mycompany
    ```
2. The artifactId is a place to specify a short name of your project. 
    ```dockerfile
    -DartifactId = oodt
    ```
3. The version indicates the initial version label for your project. 
    ```dockerfile
    -Dversion = 0.1
    ``` 
4. The oodt flag indicates the version of OODT that you want your project to be built on. N.B., this should most likely match [the most recent version of OODT](https://search.maven.org/search?q=g:org.apache.oodt).
    ```dockerfile
    -Doodt = 1.2.5
    ```

### Building OODT Docker Image
Execute below command in a prompt which is opened where the Docker file placed. Replace any thing with `oodt_docker` as it is the name of your docker image.
```dockerfile
sudo docker build -t oodt_docker . 
```    

### Running OODT Docker Image
After your docker image successfully built, execute below prompt command.`oodt_docker` is the oodt docker image name.
```dockerfile
sudo docker run -p 8080:8080 -name my_first_oodt -i -t oodt_docker
```
The -p command forwards 8080 to localhost so you can login to opsui by visiting http://localhost:8080/opsui

If you want access to the other services on the system you will also have to forward those ports locally.
