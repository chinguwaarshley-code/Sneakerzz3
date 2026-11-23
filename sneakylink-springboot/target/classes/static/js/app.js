const state = {
    role: null,
    selectedLoginRole: "CUSTOMER",
    username: null,
    shoes: [],
    cart: null,
    fulfillmentOptions: null
};

const $ = (id) => document.getElementById(id);

async function api(url, options = {}) {
    const res = await fetch(url, {
        credentials: "same-origin",
        headers: { "Content-Type": "application/json", ...(options.headers || {}) },
        ...options
    });
    const text = await res.text();
    const data = text ? JSON.parse(text) : {};
    if (!res.ok) {
        throw new Error(data.message || data.error || "Request failed");
    }
    return data;
}

function showToast(msg) {
    const t = $("toast");
    t.textContent = msg;
    t.classList.remove("hidden");
    clearTimeout(t._timer);
    t._timer = setTimeout(() => t.classList.add("hidden"), 2800);
}

function money(val) {
    return "$" + Number(val || 0).toFixed(2);
}

function openModal(id) { $(id).classList.remove("hidden"); }
function closeModal(id) { $(id).classList.add("hidden"); }

document.addEventListener("click", (e) => {
    const target = e.target.getAttribute("data-close");
    if (target) closeModal(target);
});

// ---------------- AUTH GATE ----------------
document.querySelectorAll(".role-btn").forEach((btn) => {
    btn.addEventListener("click", () => {
        document.querySelectorAll(".role-btn").forEach((b) => b.classList.remove("active"));
        btn.classList.add("active");
        state.selectedLoginRole = btn.dataset.role;
        const isMgr = state.selectedLoginRole === "MANAGER";
        $("manager-password-group").classList.toggle("hidden", !isMgr);
        $("auth-username-label").textContent = isMgr ? "Manager Name" : "Display Name (optional for guests)";
        $("auth-submit-btn").textContent = isMgr ? "Unlock Manager Portal" : "Enter Storefront";
        $("auth-error").classList.add("hidden");
    });
});

$("auth-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    $("auth-error").classList.add("hidden");
    try {
        const res = await api("/api/auth/login", {
            method: "POST",
            body: JSON.stringify({
                username: $("auth-username").value,
                role: state.selectedLoginRole,
                password: $("auth-password").value
            })
        });
        enterSession(res);
        showToast(res.message);
    } catch (err) {
        $("auth-error").textContent = err.message;
        $("auth-error").classList.remove("hidden");
    }
});

$("btn-logout").addEventListener("click", async () => {
    await api("/api/auth/logout", { method: "POST" });
    state.role = null;
    $("app-shell").classList.add("hidden");
    $("auth-gate").classList.remove("hidden");
    $("auth-password").value = "";
});

async function checkSession() {
    try {
        const me = await api("/api/auth/me");
        if (me.authenticated) {
            enterSession(me);
        }
    } catch (_) {}
}

async function enterSession(session) {
    state.role = session.role;
    state.username = session.username;

    $("auth-gate").classList.add("hidden");
    $("app-shell").classList.remove("hidden");
    $("session-user").textContent = session.username;
    $("role-badge").textContent = session.role;

    const isMgr = session.role === "MANAGER";
    $("customer-view").classList.toggle("hidden", isMgr);
    $("manager-view").classList.toggle("hidden", !isMgr);
    $("btn-open-cart").classList.toggle("hidden", isMgr);
    $("btn-my-orders").classList.toggle("hidden", isMgr);

    if (isMgr) {
        await loadAdminDashboard();
    } else {
        await loadFilterMetadata();
        await Promise.all([loadStorefrontShoes(), refreshCart()]);
    }
}

// ---------------- CUSTOMER STOREFRONT ----------------
async function loadFilterMetadata() {
    const meta = await api("/api/shoes/filters");
    $("catalog-total").textContent = meta.totalModels;
    fillSelect("filter-primary-color", meta.primaryColors, "All Colors");
    fillSelect("filter-colorway", meta.colorways, "All Colorways");
    fillSelect("filter-size-range", meta.sizeRanges, "All Size Ranges");
    fillSelect("filter-material", meta.materials, "All Materials");
    fillSelect("filter-category", meta.categories, "All Categories");
}

function fillSelect(id, items, defaultLabel) {
    const el = $(id);
    el.innerHTML = `<option value="">${defaultLabel}</option>` +
        items.map((v) => `<option value="${v}">${v}</option>`).join("");
}

async function loadStorefrontShoes() {
    const params = new URLSearchParams();
    const map = {
        query: $("filter-query").value.trim(),
        primaryColor: $("filter-primary-color").value,
        colorway: $("filter-colorway").value,
        sizeRange: $("filter-size-range").value,
        exactSize: $("filter-exact-size").value,
        material: $("filter-material").value,
        category: $("filter-category").value,
        sortBy: $("filter-sort").value
    };
    Object.entries(map).forEach(([k, v]) => { if (v) params.set(k, v); });
    if ($("filter-instock").checked) params.set("inStockOnly", "true");

    state.shoes = await api("/api/shoes?" + params.toString());
    $("results-count").textContent = `Showing ${state.shoes.length} footwear model(s)`;
    renderShoeGrid();
}

function renderShoeGrid() {
    const grid = $("shoe-grid");
    if (!state.shoes.length) {
        grid.innerHTML = `<div class="receipt-box">No footwear matches those filters. Try clearing one or more criteria.</div>`;
        return;
    }
    grid.innerHTML = state.shoes.map((s) => `
        <article class="shoe-card">
            <img class="shoe-thumb" src="${s.imageUrl}" alt="${s.name}">
            <div class="shoe-body">
                <div class="shoe-meta-top">
                    <span>${s.brand} &middot; ${s.category}</span>
                    <span class="${s.stockQuantity > 0 ? "badge-green" : "badge-red"} badge">${s.stockQuantity > 0 ? s.stockQuantity + " in stock" : "Out of Stock"}</span>
                </div>
                <div class="shoe-title">${s.name}</div>
                <div class="shoe-tags">
                    <span class="tag">${s.colorway}</span>
                    <span class="tag">${s.sizeRange}</span>
                    <span class="tag">${s.material}</span>
                </div>
                <div class="shoe-footer">
                    <span class="price">${money(s.price)}</span>
                    <button class="btn btn-primary btn-sm" onclick="openShoeDetail(${s.id})" ${s.stockQuantity <= 0 ? "disabled" : ""}>
                        ${s.stockQuantity > 0 ? "Select Size" : "Sold Out"}
                    </button>
                </div>
            </div>
        </article>
    `).join("");
}

["filter-primary-color", "filter-colorway", "filter-size-range", "filter-exact-size", "filter-material", "filter-category", "filter-sort", "filter-instock"]
    .forEach((id) => $(id).addEventListener("change", loadStorefrontShoes));

$("filter-query").addEventListener("input", () => {
    clearTimeout($("filter-query")._deb);
    $("filter-query")._deb = setTimeout(loadStorefrontShoes, 220);
});

$("btn-reset-filters").addEventListener("click", () => {
    ["filter-query", "filter-primary-color", "filter-colorway", "filter-size-range", "filter-exact-size", "filter-material", "filter-category"]
        .forEach((id) => $(id).value = "");
    $("filter-sort").value = "featured";
    $("filter-instock").checked = false;
    loadStorefrontShoes();
});

window.openShoeDetail = function(shoeId) {
    const shoe = state.shoes.find((s) => s.id === shoeId);
    if (!shoe) return;
    const sizes = shoe.availableSizes.split(",").map((x) => x.trim()).filter(Boolean);
    let chosenSize = sizes[Math.floor(sizes.length / 2)] || sizes[0];

    $("shoe-modal-body").innerHTML = `
        <img class="shoe-thumb" style="border-radius:10px;margin-bottom:14px;height:210px;" src="${shoe.imageUrl}" alt="${shoe.name}">
        <div class="shoe-meta-top"><span>${shoe.brand} &middot; ${shoe.sku}</span><span>${shoe.stockQuantity} Available</span></div>
        <h3 style="margin:6px 0 4px;">${shoe.name}</h3>
        <p style="color:var(--muted);font-size:14px;margin-bottom:10px;">${shoe.description || ""}</p>
        <div class="shoe-tags" style="margin-bottom:12px;">
            <span class="tag">Colorway: ${shoe.colorway}</span>
            <span class="tag">Material: ${shoe.material}</span>
            <span class="tag">Range: ${shoe.sizeRange}</span>
        </div>
        <strong style="font-size:13px;">Choose US Size:</strong>
        <div class="size-pills" id="modal-size-pills">
            ${sizes.map((sz) => `<button type="button" class="size-pill ${sz === chosenSize ? "selected" : ""}" data-size="${sz}">US ${sz}</button>`).join("")}
        </div>
        <div class="shoe-footer">
            <span class="price">${money(shoe.price)}</span>
            <button id="modal-add-btn" class="btn btn-primary">Add to Persistent Cart</button>
        </div>
    `;

    $("modal-size-pills").querySelectorAll(".size-pill").forEach((pill) => {
        pill.addEventListener("click", () => {
            $("modal-size-pills").querySelectorAll(".size-pill").forEach((p) => p.classList.remove("selected"));
            pill.classList.add("selected");
            chosenSize = pill.dataset.size;
        });
    });

    $("modal-add-btn").addEventListener("click", async () => {
        try {
            state.cart = await api("/api/cart/items", {
                method: "POST",
                body: JSON.stringify({ shoeId: shoe.id, selectedSize: chosenSize, quantity: 1 })
            });
            renderCart();
            closeModal("shoe-modal");
            showToast(`Added ${shoe.name} (US ${chosenSize}) to cart`);
        } catch (err) {
            showToast(err.message);
        }
    });

    openModal("shoe-modal");
};

// ---------------- PERSISTENT CART ----------------
async function refreshCart() {
    state.cart = await api("/api/cart");
    renderCart();
}

function renderCart() {
    const c = state.cart || { items: [], totalQuantity: 0, subtotal: 0, standardShippingFee: 0 };
    $("cart-count-pill").textContent = c.totalQuantity;
    $("cart-subtotal").textContent = money(c.subtotal);
    $("cart-shipping").textContent = c.standardShippingFee === 0 ? "FREE" : money(c.standardShippingFee);

    const list = $("cart-items-list");
    if (!c.items.length) {
        list.innerHTML = `<p style="color:var(--muted);font-size:14px;">Your persistent cart is empty.</p>`;
        $("btn-start-checkout").disabled = true;
        return;
    }
    $("btn-start-checkout").disabled = false;
    list.innerHTML = c.items.map((item) => `
        <div class="cart-row">
            <img src="${item.shoe.imageUrl}" alt="${item.shoe.name}">
            <div class="cart-row-info">
                <strong>${item.shoe.name}</strong>
                <div style="color:var(--muted);">Size US ${item.selectedSize} &middot; ${money(item.shoe.price)}</div>
                <div class="qty-controls">
                    <button class="btn btn-outline btn-sm" onclick="updateCartQty(${item.id}, ${item.quantity - 1})">-</button>
                    <span>${item.quantity}</span>
                    <button class="btn btn-outline btn-sm" onclick="updateCartQty(${item.id}, ${item.quantity + 1})">+</button>
                    <button class="btn btn-danger btn-sm" onclick="removeCartItem(${item.id})">Remove</button>
                </div>
            </div>
        </div>
    `).join("");
}

window.updateCartQty = async function(itemId, newQty) {
    try {
        if (newQty <= 0) {
            state.cart = await api(`/api/cart/items/${itemId}`, { method: "DELETE" });
        } else {
            state.cart = await api(`/api/cart/items/${itemId}`, {
                method: "PUT",
                body: JSON.stringify({ quantity: newQty })
            });
        }
        renderCart();
    } catch (err) {
        showToast(err.message);
    }
};

window.removeCartItem = async function(itemId) {
    state.cart = await api(`/api/cart/items/${itemId}`, { method: "DELETE" });
    renderCart();
};

$("btn-open-cart").addEventListener("click", () => openModal("cart-drawer"));
$("btn-clear-cart").addEventListener("click", async () => {
    state.cart = await api("/api/cart", { method: "DELETE" });
    renderCart();
    showToast("Cart cleared");
});

// ---------------- CHECKOUT & DYNAMIC FULFILLMENT ----------------
$("btn-start-checkout").addEventListener("click", async () => {
    closeModal("cart-drawer");
    state.fulfillmentOptions = await api("/api/orders/fulfillment-options");
    $("co-name").value = state.username || "";
    $("co-pickup-window").innerHTML = state.fulfillmentOptions.pickupWindows
        .map((w) => `<option value="${w}">${w}</option>`).join("");
    updateFulfillmentUI();
    openModal("checkout-modal");
});

$("co-fulfillment").addEventListener("change", updateFulfillmentUI);

function updateFulfillmentUI() {
    const mode = $("co-fulfillment").value;
    const opt = state.fulfillmentOptions;
    if (!opt) return;

    const isPickup = mode === "IN_STORE_PICKUP";
    $("shipping-fields").classList.toggle("hidden", isPickup);
    $("pickup-fields").classList.toggle("hidden", !isPickup);

    if (mode === "STANDARD_DELIVERY") {
        $("fulfillment-preview").innerHTML = `<strong>Estimated Standard Delivery:</strong> ${opt.standardDeliveryLabel} &middot; Fee: <strong>${opt.standardShippingFee === 0 ? "FREE (Order $250+)" : money(opt.standardShippingFee)}</strong>`;
    } else if (mode === "EXPRESS_DELIVERY") {
        $("fulfillment-preview").innerHTML = `<strong>Express Air Delivery:</strong> ${opt.expressDeliveryLabel} &middot; Fee: <strong>${money(opt.expressShippingFee)}</strong>`;
    } else {
        $("fulfillment-preview").innerHTML = `<strong>In-Store Pickup (FREE):</strong> ${opt.storeName}, ${opt.storeAddress}`;
    }
}

$("checkout-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    $("checkout-error").classList.add("hidden");
    try {
        const order = await api("/api/orders/checkout", {
            method: "POST",
            body: JSON.stringify({
                customerName: $("co-name").value,
                customerEmail: $("co-email").value,
                customerPhone: $("co-phone").value,
                fulfillmentType: $("co-fulfillment").value,
                shippingAddress: $("co-address").value,
                shippingCity: $("co-city").value,
                shippingPostalCode: $("co-postal").value,
                selectedPickupWindow: $("co-pickup-window").value,
                paymentMethod: $("co-payment-method").value,
                cardNumber: $("co-card").value
            })
        });
        closeModal("checkout-modal");
        await Promise.all([refreshCart(), loadStorefrontShoes()]);
        renderOrderReceipt(order);
    } catch (err) {
        $("checkout-error").textContent = err.message;
        $("checkout-error").classList.remove("hidden");
    }
});

function renderOrderReceipt(order) {
    $("orders-modal-title").textContent = `Order Confirmed: ${order.orderNumber}`;
    const schedule = order.fulfillmentType === "IN_STORE_PICKUP"
        ? `Pickup Window: <strong>${order.pickupWindowLabel}</strong>`
        : `Estimated Delivery: <strong>${order.deliveryWindowLabel}</strong>`;
    $("orders-modal-body").innerHTML = `
        <div class="receipt-box">
            <p><strong>Status:</strong> ${order.status}</p>
            <p>${schedule}</p>
            <p><strong>Destination:</strong> ${order.shippingAddress || ""} ${order.shippingCity || ""} ${order.shippingPostalCode || ""}</p>
            <p><strong>Payment:</strong> ${order.paymentMethod} ${order.paymentLast4 !== "N/A" ? "(ending " + order.paymentLast4 + ")" : ""}</p>
            <hr style="border-color:var(--border);margin:10px 0;">
            ${order.items.map((i) => `<div class="summary-row"><span>${i.quantity}x ${i.shoeName} (US ${i.selectedSize})</span><span>${money(i.unitPrice * i.quantity)}</span></div>`).join("")}
            <hr style="border-color:var(--border);margin:10px 0;">
            <div class="summary-row"><span>Total Paid</span><strong>${money(order.totalAmount)}</strong></div>
        </div>
    `;
    openModal("orders-modal");
}

$("btn-my-orders").addEventListener("click", async () => {
    const orders = await api("/api/orders");
    $("orders-modal-title").textContent = "Your Order History";
    $("orders-modal-body").innerHTML = orders.length
        ? orders.map((o) => `
            <div class="receipt-box">
                <div class="summary-row"><strong>${o.orderNumber}</strong><span class="badge badge-blue">${o.status}</span></div>
                <div style="color:var(--muted);margin:4px 0;">${o.fulfillmentType === "IN_STORE_PICKUP" ? "Pickup: " + o.pickupWindowLabel : "Delivery: " + o.deliveryWindowLabel}</div>
                <div class="summary-row"><span>${o.items.length} line item(s)</span><strong>${money(o.totalAmount)}</strong></div>
            </div>
        `).join("")
        : `<p style="color:var(--muted);">No orders placed yet.</p>`;
    openModal("orders-modal");
});

// ---------------- MANAGER ADMIN PORTAL ----------------
async function loadAdminDashboard() {
    const [stats, shoes, orders] = await Promise.all([
        api("/api/admin/stats"),
        api("/api/shoes"),
        api("/api/admin/orders")
    ]);
    state.shoes = shoes;
    $("admin-stats").innerHTML = `
        <div class="stat-card"><div class="stat-label">Shoe Models</div><div class="stat-value">${stats.totalModels}</div></div>
        <div class="stat-card"><div class="stat-label">Units in Stock</div><div class="stat-value">${stats.totalUnitsInStock}</div></div>
        <div class="stat-card"><div class="stat-label">Low Stock (&le;5)</div><div class="stat-value">${stats.lowStockModels}</div></div>
        <div class="stat-card"><div class="stat-label">Total Orders</div><div class="stat-value">${stats.totalOrders}</div></div>
        <div class="stat-card"><div class="stat-label">Revenue</div><div class="stat-value">${money(stats.totalRevenue)}</div></div>
    `;
    renderAdminInventory(shoes);
    renderAdminOrders(orders);
}

function renderAdminInventory(list) {
    $("admin-shoe-tbody").innerHTML = list.map((s) => `
        <tr>
            <td><img class="mini-thumb" src="${s.imageUrl}" alt="${s.name}"></td>
            <td><strong>${s.name}</strong><div style="color:var(--muted);font-size:12px;">${s.sku} &middot; ${s.brand}</div></td>
            <td>${s.colorway}<div style="color:var(--muted);font-size:12px;">${s.material}</div></td>
            <td>${s.sizeRange}</td>
            <td><strong>${money(s.price)}</strong></td>
            <td>
                <div class="stock-inline">
                    <input type="number" min="0" id="stk-${s.id}" value="${s.stockQuantity}">
                    <button class="btn btn-ghost btn-sm" onclick="saveQuickStock(${s.id})">Save</button>
                </div>
            </td>
            <td>
                <button class="btn btn-outline btn-sm" onclick="openAdminShoeModal(${s.id})">Edit / Image</button>
                <button class="btn btn-danger btn-sm" onclick="deleteAdminShoe(${s.id})">Delete</button>
            </td>
        </tr>
    `).join("");
}

function renderAdminOrders(orders) {
    $("admin-orders-tbody").innerHTML = orders.length
        ? orders.map((o) => `
            <tr>
                <td><strong>${o.orderNumber}</strong></td>
                <td>${o.customerName}<div style="color:var(--muted);font-size:12px;">${o.customerEmail}</div></td>
                <td>${o.fulfillmentType}<div style="color:var(--cyan);font-size:12px;">${o.pickupWindowLabel || o.deliveryWindowLabel || ""}</div></td>
                <td>${o.items.map((i) => `${i.quantity}x ${i.shoeName} (US ${i.selectedSize})`).join("<br>")}</td>
                <td><strong>${money(o.totalAmount)}</strong></td>
                <td><span class="badge badge-blue">${o.status}</span></td>
            </tr>
        `).join("")
        : `<tr><td colspan="6" style="color:var(--muted);">No customer orders recorded yet.</td></tr>`;
}

$("admin-search").addEventListener("input", () => {
    const q = $("admin-search").value.trim().toLowerCase();
    const filtered = state.shoes.filter((s) =>
        `${s.name} ${s.sku} ${s.brand} ${s.colorway} ${s.material}`.toLowerCase().includes(q)
    );
    renderAdminInventory(filtered);
});

$("btn-tab-inventory").addEventListener("click", () => {
    $("admin-inventory-panel").classList.remove("hidden");
    $("admin-orders-panel").classList.add("hidden");
});

$("btn-tab-orders").addEventListener("click", () => {
    $("admin-inventory-panel").classList.add("hidden");
    $("admin-orders-panel").classList.remove("hidden");
});

window.saveQuickStock = async function(id) {
    const qty = parseInt($(`stk-${id}`).value, 10);
    try {
        await api(`/api/admin/shoes/${id}/stock`, {
            method: "PATCH",
            body: JSON.stringify({ stockQuantity: qty })
        });
        showToast("Stock updated");
        await loadAdminDashboard();
    } catch (err) {
        showToast(err.message);
    }
};

window.deleteAdminShoe = async function(id) {
    if (!confirm("Delete this footwear model from inventory?")) return;
    await api(`/api/admin/shoes/${id}`, { method: "DELETE" });
    showToast("Model removed");
    await loadAdminDashboard();
};

$("btn-new-shoe").addEventListener("click", () => openAdminShoeModal(null));

window.openAdminShoeModal = function(id) {
    $("admin-shoe-error").classList.add("hidden");
    $("as-imageFile").value = "";
    if (!id) {
        $("admin-shoe-modal-title").textContent = "Add New Shoe Model";
        $("as-id").value = "";
        ["as-sku", "as-name", "as-brand", "as-category", "as-colorway", "as-primaryColor", "as-sizeRange", "as-availableSizes", "as-material", "as-price", "as-stockQuantity", "as-imageUrl", "as-description"]
            .forEach((f) => $(f).value = "");
        $("as-featured").value = "false";
    } else {
        const s = state.shoes.find((x) => x.id === id);
        $("admin-shoe-modal-title").textContent = `Edit ${s.name}`;
        $("as-id").value = s.id;
        $("as-sku").value = s.sku;
        $("as-name").value = s.name;
        $("as-brand").value = s.brand;
        $("as-category").value = s.category;
        $("as-colorway").value = s.colorway;
        $("as-primaryColor").value = s.primaryColor;
        $("as-sizeRange").value = s.sizeRange;
        $("as-availableSizes").value = s.availableSizes;
        $("as-material").value = s.material;
        $("as-price").value = s.price;
        $("as-stockQuantity").value = s.stockQuantity;
        $("as-imageUrl").value = s.imageUrl;
        $("as-description").value = s.description || "";
        $("as-featured").value = String(!!s.featured);
    }
    openModal("admin-shoe-modal");
};

$("as-imageFile").addEventListener("change", (e) => {
    const file = e.target.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => { $("as-imageUrl").value = reader.result; };
    reader.readAsDataURL(file);
});

$("admin-shoe-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    $("admin-shoe-error").classList.add("hidden");
    const id = $("as-id").value;
    const payload = {
        sku: $("as-sku").value,
        name: $("as-name").value,
        brand: $("as-brand").value,
        category: $("as-category").value,
        colorway: $("as-colorway").value,
        primaryColor: $("as-primaryColor").value,
        sizeRange: $("as-sizeRange").value,
        availableSizes: $("as-availableSizes").value,
        material: $("as-material").value,
        price: parseFloat($("as-price").value),
        stockQuantity: parseInt($("as-stockQuantity").value, 10),
        imageUrl: $("as-imageUrl").value,
        description: $("as-description").value,
        featured: $("as-featured").value === "true"
    };
    try {
        await api(id ? `/api/admin/shoes/${id}` : "/api/admin/shoes", {
            method: id ? "PUT" : "POST",
            body: JSON.stringify(payload)
        });
        closeModal("admin-shoe-modal");
        showToast(id ? "Product updated" : "New shoe model added");
        await loadAdminDashboard();
    } catch (err) {
        $("admin-shoe-error").textContent = err.message;
        $("admin-shoe-error").classList.remove("hidden");
    }
});

checkSession();
