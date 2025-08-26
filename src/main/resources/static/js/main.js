document.addEventListener("DOMContentLoaded", function () {

    const sidebar = document.getElementById("sidebar");
    const toggleBtn = document.getElementById("sidebarToggle");

    if (sidebar && toggleBtn) {
        toggleBtn.addEventListener("click", () => {
            // Toggle hidden class for mobile
            sidebar.classList.toggle("-translate-x-full");

            // Optionally store state in localStorage
            if (!sidebar.classList.contains("-translate-x-full")) {
                localStorage.setItem("sidebarOpen", "true");
            } else {
                localStorage.setItem("sidebarOpen", "false");
            }
        });

        // Restore last state from localStorage
        if (localStorage.getItem("sidebarOpen") === "true") {
            sidebar.classList.remove("-translate-x-full");
        }
    }
});



$(document).on('change', '#checkAll', function() {
    $('.rowCheckbox').prop('checked', this.checked);
    updateSelectedCount();
});

// Individual checkbox change
$(document).on('change', '.rowCheckbox', function() {
    var allChecked = $('.rowCheckbox:checked').length === $('.rowCheckbox').length;
    $('#checkAll').prop('checked', allChecked);
    updateSelectedCount();
});

// Update selected count and button state
function updateSelectedCount() {
    const count = $('.rowCheckbox:checked').length;
    $('#selectedCount').text(count);
    $('#bulkDeleteBtn').prop('disabled', count === 0);
    $('#bulkDeleteBtn').toggleClass('opacity-50 cursor-not-allowed', count === 0);
}


// =====================
// Chart.js Example Setup
// =====================
if (typeof Chart !== "undefined") {
    const ctx = document.getElementById("trafficChart");
    if (ctx) {
        new Chart(ctx, {
            type: "line",
            data: {
                labels: ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul"],
                datasets: [{
                    label: "Visitors",
                    data: [320, 450, 300, 500, 600, 750, 900],
                    borderColor: "#3b82f6",
                    backgroundColor: "rgba(59, 130, 246, 0.2)",
                    fill: true,
                    tension: 0.4
                }]
            },
            options: {
                responsive: true,
                plugins: { legend: { display: true } }
            }
        });
    }
}

// =====================
// Ripple Effect on Buttons
// =====================
document.querySelectorAll(".btn-ripple").forEach(button => {
    button.addEventListener("click", function (e) {
        const circle = document.createElement("span");
        const diameter = Math.max(this.clientWidth, this.clientHeight);
        const radius = diameter / 2;

        circle.style.width = circle.style.height = `${diameter}px`;
        circle.style.left = `${e.clientX - this.offsetLeft - radius}px`;
        circle.style.top = `${e.clientY - this.offsetTop - radius}px`;
        circle.classList.add("ripple");

        const ripple = this.getElementsByClassName("ripple")[0];
        if (ripple) ripple.remove();

        this.appendChild(circle);
    });
});
