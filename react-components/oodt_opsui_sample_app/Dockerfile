FROM node:16-alpine as builder
COPY . /src
WORKDIR /src
RUN npm install
RUN npm run build

FROM node:16-alpine
RUN npm install -g serve
COPY --from=builder /src/build /opsui
WORKDIR /opsui
ENTRYPOINT ["serve", "-s"]