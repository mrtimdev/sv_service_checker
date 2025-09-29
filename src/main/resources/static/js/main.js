window.addEventListener('load', function () {
    document.getElementById('preloader').classList.add('hidden');
});
document.addEventListener("DOMContentLoaded", function () {
    
    if (window.innerWidth < 640) {
        localStorage.setItem("sidebarOpen", "false");
    } 

    const sidebar = document.getElementById("sidebar");
    const toggleBtn = document.getElementById("sidebarToggle");
     const overlay = document.getElementById('sidebarOverlay');
    const mainContent = document.getElementById('main-content');

    if (sidebar && toggleBtn) {
        toggleBtn.addEventListener("click", () => {
            // Toggle hidden class for mobile
            sidebar.classList.toggle("-translate-x-full");

            // Optionally store state in localStorage
            if (!sidebar.classList.contains("-translate-x-full")) {
                localStorage.setItem("sidebarOpen", "true");
                mainContent.classList.add('sidebar-open');
                $('#sidebarOverlay').addClass('active');
            } else {
                localStorage.setItem("sidebarOpen", "false");
                mainContent.classList.remove('sidebar-open');
                $('#sidebarOverlay').removeClass('active');
            }
        });

        // Restore last state from localStorage
        if (localStorage.getItem("sidebarOpen") === "true") {
            sidebar.classList.remove("-translate-x-full");
            mainContent.classList.add('sidebar-open');
            console.log("sidebarOpen True");
        } else {
            sidebar.classList.add("-translate-x-full");
            mainContent.classList.remove('sidebar-open');
            console.log("sidebarOpen False");
        }

        overlay?.addEventListener('click', () => {
            sidebar.classList.add("-translate-x-full");
            mainContent.classList.remove('sidebar-open');
            console.log("sidebarOpen False");
            $('#sidebarOverlay').removeClass('active');
            localStorage.setItem("sidebarOpen", "false");
        });
    }

    
});


// document.addEventListener('DOMContentLoaded', () => {
//   const sidebar = document.getElementById('sidebar');
//   const overlay = document.getElementById('sidebarOverlay');
//   const toggleBtn = document.getElementById('sidebarToggle');
//   const mainContent = document.getElementById('main-content');

//   function openSidebar() {
//     console.log("open");
//     sidebar.classList.remove('-translate-x-full');
//     sidebar.classList.add('translate-x-0');
//     overlay?.classList.remove('hidden');
//     // add class to main content for margin-left animation
//     mainContent.classList.add('sidebar-open');
//   }

//   function closeSidebar() {
//     console.log("close");
//     sidebar.classList.add('-translate-x-full');
//     sidebar.classList.remove('translate-x-0');
//     overlay?.classList.add('hidden');
//     // remove class from main content
//     mainContent.classList.remove('sidebar-open');
//   }

//   toggleBtn.addEventListener('click', () => {
//     if (!sidebar.classList.contains('-translate-x-full')) {
//       closeSidebar();
//       console.log("close");
//     } else {
      
//       openSidebar();
//       console.log("open");
//     }
//   });

// //   overlay.addEventListener('click', closeSidebar);

//   // Ensure proper state when resizing
//   window.addEventListener('resize', () => {
//     if (window.innerWidth > 640) {
//       // keep sidebar visible, main-content open
//       sidebar.classList.remove('-translate-x-full');
//       sidebar.classList.add('translate-x-0');
//       mainContent.classList.add('sidebar-open');
//       overlay?.classList.add('hidden');
//     } else {
//       // mobile defaults closed
//       sidebar.classList.add('-translate-x-full');
//       sidebar.classList.remove('translate-x-0');
//       mainContent.classList.remove('sidebar-open');
//     }
//   });
// });



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
