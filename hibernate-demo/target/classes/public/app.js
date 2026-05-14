const apiBase = "/api/products";
const form = document.getElementById("product-form");
const formTitle = document.getElementById("form-title");
const cancelEditBtn = document.getElementById("cancel-edit");
const statusNote = document.getElementById("form-status");
const productBody = document.getElementById("product-body");
const searchInput = document.getElementById("search");

let editId = null;
let allProducts = [];

const errors = {
  name: document.querySelector("[data-error-for='name']"),
  category: document.querySelector("[data-error-for='category']"),
  price: document.querySelector("[data-error-for='price']"),
};

const fields = {
  name: document.getElementById("name"),
  category: document.getElementById("category"),
  price: document.getElementById("price"),
};

function setStatus(message, tone = "") {
  statusNote.textContent = message;
  statusNote.className = `form-note ${tone}`.trim();
}

function clearErrors() {
  Object.values(errors).forEach((el) => (el.textContent = ""));
}

function validate() {
  clearErrors();
  let ok = true;
  if (fields.name.value.trim().length < 2) {
    errors.name.textContent = "Please enter at least 2 characters.";
    ok = false;
  }
  if (fields.category.value.trim().length < 2) {
    errors.category.textContent = "Please enter at least 2 characters.";
    ok = false;
  }
  if (!fields.price.value || Number(fields.price.value) <= 0) {
    errors.price.textContent = "Price must be greater than 0.";
    ok = false;
  }
  return ok;
}

function resetForm() {
  form.reset();
  editId = null;
  formTitle.textContent = "Add New Product";
  cancelEditBtn.style.display = "none";
}

async function fetchProducts() {
  const response = await fetch(apiBase);
  allProducts = await response.json();
  renderProducts(allProducts);
}

function renderProducts(products) {
  productBody.innerHTML = "";
  if (!Array.isArray(products)) {
    return;
  }
  products.forEach((product) => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${product.id}</td>
      <td>${product.name}</td>
      <td>${product.category}</td>
      <td>₹${Number(product.price).toFixed(2)}</td>
      <td>
        <button class="action-btn" data-action="edit" data-id="${product.id}">Edit</button>
        <button class="action-btn delete" data-action="delete" data-id="${product.id}">Delete</button>
      </td>
    `;
    productBody.appendChild(row);
  });
}

function findProduct(id) {
  return allProducts.find((item) => item.id === id);
}

async function saveProduct(payload) {
  const url = editId ? `${apiBase}/${editId}` : apiBase;
  const method = editId ? "PUT" : "POST";
  const response = await fetch(url, {
    method,
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error || "Unable to save product");
  }

  return response.json();
}

async function deleteProduct(id) {
  const response = await fetch(`${apiBase}/${id}`, { method: "DELETE" });
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error || "Unable to delete product");
  }
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  setStatus("");

  if (!validate()) {
    return;
  }

  const payload = {
    name: fields.name.value.trim(),
    category: fields.category.value.trim(),
    price: Number(fields.price.value),
  };

  try {
    await saveProduct(payload);
    setStatus(editId ? "Product updated." : "Product added.", "success");
    resetForm();
    await fetchProducts();
  } catch (error) {
    setStatus(error.message, "error");
  }
});

productBody.addEventListener("click", async (event) => {
  const button = event.target.closest("button");
  if (!button) {
    return;
  }
  const action = button.dataset.action;
  const id = Number(button.dataset.id);

  if (action === "edit") {
    const product = findProduct(id);
    if (!product) {
      return;
    }
    editId = product.id;
    fields.name.value = product.name;
    fields.category.value = product.category;
    fields.price.value = product.price;
    formTitle.textContent = "Edit Product";
    cancelEditBtn.style.display = "inline-flex";
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  if (action === "delete") {
    if (!confirm("Delete this product?")) {
      return;
    }
    try {
      await deleteProduct(id);
      await fetchProducts();
    } catch (error) {
      setStatus(error.message, "error");
    }
  }
});

cancelEditBtn.addEventListener("click", () => {
  resetForm();
});

searchInput.addEventListener("input", (event) => {
  const value = event.target.value.toLowerCase();
  if (!value) {
    renderProducts(allProducts);
    return;
  }
  const filtered = allProducts.filter((item) =>
    item.name.toLowerCase().includes(value) || item.category.toLowerCase().includes(value)
  );
  renderProducts(filtered);
});

resetForm();
fetchProducts().catch((error) => {
  setStatus(error.message, "error");
});
