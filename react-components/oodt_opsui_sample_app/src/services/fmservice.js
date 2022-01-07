import { fmconnection } from "constants/connection";


export const getAllProductTypes = () => {
  return new Promise((resolve, reject) => {
    fmconnection
      .get("/productTypes")
      .then((res) => {
        const { productTypes } = res.data.productTypeList;
        resolve(productTypes);
      })
      .catch((err) => {
        reject(err);
      });
  });
};

export const getProductById = (productId) => {
  return new Promise((resolve, reject) => {
    fmconnection
      .get("/product", {
        params: { productId },
      })
      .then((res) => {
        const productData = res.data.product;
        const metadataArr = productData.metadata.keyval;
        const metadataInfoObj = {};
        for (let i = 0, len = metadataArr.length; i < len; i++) {
          metadataInfoObj[metadataArr[i].key] = metadataArr[i].val;
        }
        productData["metadata"] = metadataInfoObj;
        productData["references"] = productData.references.reference
        resolve(productData);
      })
      .catch((err) => {
        reject(err);
      });
  });
};

export const getProductPage = (productParams) => {
  const {productType,productName, productPageNo} = productParams;
  return new Promise((resolve, reject) => {
    fmconnection
      .get("/products", {
        params: {
          productTypeName: productType,
          productName,
          currentProductPage: productPageNo
        },
      })
      .then((res) => {
        resolve(res.data.productPage);
      })
      .catch((error) => {
        reject(error);
      });
  });
};

export const ingestProduct = (
  product,
  productType,
  productStructure,
  onUploadProgress
) => {
  return new Promise((resolve, reject) => {
    fmconnection
      .post("/productWithFile", product, {
        params: {
          productType,
          productStructure,
        },
        onUploadProgress,
      })
      .then((result) => {
        resolve({ productId: result.data });
      })
      .catch((error) => {
        reject(error);
      });
  });
};

export const ingestProductWithMetaFile = (files,onUploadProgress) => {
  return new Promise((resolve,reject) => {
    fmconnection.post("/productWithMeta",files,{
      onUploadProgress
    }).then(res => {
      resolve({productId: res.data})
    }).catch(err => {
      reject(err)
    })
  })
}

export const removeProductById = (productId) => {
  return new Promise((resolve, reject) => {
    fmconnection
      .delete("/removeProduct", {
        params: { productId },
      })
      .then((res) => {
        resolve(true);
      })
      .catch((err) => {
        reject(err);
      });
  });
};