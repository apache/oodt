# OODT Docker

Automated RADIX build on Docker for Apache OODT

## Usage

You should edit docker file replacing the parameters below with values you desire before building this image.

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
Execute below command in a prompt which is opened where the Docker file placed. Replace  `oodt_docker` with any name as it will be the name of your docker image.
```dockerfile
sudo docker build -t oodt_docker . 
```    

### Running OODT Docker Image
After your docker image successfully built, execute below prompt command.`oodt_docker` is the oodt docker image name.
```dockerfile
sudo docker run -p 8080:8080 -p 9000:9000 -p 9001:9001 -p 9002:9002 -p 2001:2001 -p 9200:9200  --name my_first_oodt -i -t oodt_docker
```
