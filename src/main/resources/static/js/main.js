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



function statusFormat(status) {
    if (!status) return "";

    switch (status) {
        case "APPROVED":
            return `<span class="px-2 py-1 text-xs font-medium text-white bg-green-500 rounded-md">
                        Approved
                    </span>`;
        case "REJECTED":
            return `<span class="px-2 py-1 text-xs font-medium text-white bg-red-500 rounded-md">
                        Rejected
                    </span>`;
        case "PENDING":
            return `<span class="px-2 py-1 text-xs font-medium text-white bg-yellow-500 rounded-md">
                        Pending
                    </span>`;
        case "CANCELLED":
            return `<span class="px-2 py-1 text-xs font-medium text-white bg-gray-400 rounded-md">
                        Cancelled
                    </span>`;
        case "INREVIEW":
            return `<span class="px-2 py-1 text-xs font-medium text-white bg-blue-500 rounded-md">
                        In Review
                    </span>`;
        default:
            return `<span class="px-2 py-1 text-xs font-medium text-gray-700 bg-gray-200 rounded-md">
                        ${status}
                    </span>`;
    }
}

function formatDate(dateStr, options = { showTime: false, showAgo: false }) {
    if (!dateStr) return "";

    const date = new Date(dateStr);

    let formatted = date.toLocaleDateString("en-GB", {  
        year: "numeric",
        month: "short",
        day: "2-digit"
    });

    if (options.showTime) {
        const time = date.toLocaleTimeString("en-GB", {
            hour: "2-digit",
            minute: "2-digit",
            second: "2-digit",
            hour12: true
        });
        formatted += `, ${time}`;
    }

    if (options.showAgo) {
        formatted += ` ${dayjs(dateStr).fromNow()}`;
    }

    return formatted;
}
