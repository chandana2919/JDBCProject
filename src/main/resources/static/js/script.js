// Fetch databases for dropdown
async function fetchDatabases() {
    resetForm(); // Clear input fields and checkboxes on page load

    try {
        const response = await fetch('/api/db-dropdown');
        if (response.ok) {
            const databaseList = await response.json();
            const optionsContainer = document.getElementById('databaseOptions');

            // Clear existing options
            optionsContainer.innerHTML = '';

            // Populate dropdown with database options
            databaseList.forEach(db => {
                const label = document.createElement('label');
                label.innerHTML = `<input type="checkbox" class="db-checkbox" value="${db.index}"> ${db.dbName}`;
                optionsContainer.appendChild(label);

                // Bind onchange event to each checkbox
                label.querySelector('.db-checkbox').onchange = updateSelectedCount;
            });

            // Toggle dropdown content visibility on button click
            document.getElementById('dropdownBtn').onclick = function () {
                const dropdownContent = document.getElementById('dropdownContent');
                dropdownContent.style.display = dropdownContent.style.display === 'block' ? 'none' : 'block';
            };

            // Select All functionality
            const selectAllCheckbox = document.getElementById('selectAll');
            selectAllCheckbox.onchange = function () {
                const checkboxes = document.querySelectorAll('.db-checkbox');
                checkboxes.forEach(checkbox => {
                    checkbox.checked = selectAllCheckbox.checked; // Check or uncheck all
                });
                updateSelectedCount(); // Update selected count
            };

            // Initial count update
            updateSelectedCount();
        } else {
            console.error('Failed to fetch database options');
            document.getElementById('message').innerText = 'Error fetching database options.';
        }
    } catch (error) {
        console.error('Error fetching databases:', error);
        document.getElementById('message').innerText = 'Error fetching databases: ' + error.message;
    }
}

// Reset form fields and checkboxes
function resetForm() {
    document.getElementById('sqlInput').value = ''; // Clear SQL input
    const checkboxes = document.querySelectorAll('.db-checkbox');
    checkboxes.forEach(checkbox => {
        checkbox.checked = false; // Uncheck all checkboxes
    });
    const selectAllCheckbox = document.getElementById('selectAll');
    selectAllCheckbox.checked = false; // Uncheck the "Select All" checkbox
    updateSelectedCount(); // Update count display
}

// Function to update the selected count display
function updateSelectedCount() {
    const checkboxes = document.querySelectorAll('.db-checkbox');
    const selectedCount = Array.from(checkboxes).filter(checkbox => checkbox.checked).length;
    document.getElementById('selectedCount').innerText = `${selectedCount} selected`; // Update the display

    // Debugging
    console.log(`Selected count: ${selectedCount}`);
    updateButtonState(); // Update button state based on selected count
}

// Function to update the state of buttons based on SQL query and database selection
function updateButtonState() {
    const sqlInput = document.getElementById('sqlInput').value.trim().toLowerCase();
    const downloadBtn = document.getElementById('downloadBtn');
    const updateBtn = document.getElementById('updateBtn');
    const queryBtn = document.getElementById('queryBtn');

    // Check if at least one database is selected
    const selectedCheckboxes = document.querySelectorAll('.db-checkbox:checked');
    const isDatabaseSelected = selectedCheckboxes.length === 1; // Must be exactly one

    // Enable Download button for SELECT queries
    downloadBtn.disabled = !sqlInput.startsWith('select');
    downloadBtn.classList.toggle('enabled', sqlInput.startsWith('select'));
    downloadBtn.classList.toggle('disabled', !sqlInput.startsWith('select'));

    // Enable Update button for UPDATE, TRUNCATE, INSERT queries
    const isUpdateQuery = sqlInput.startsWith('update') || sqlInput.startsWith('truncate') || sqlInput.startsWith('insert');
    updateBtn.disabled = !isUpdateQuery;
    updateBtn.classList.toggle('enabled', isUpdateQuery);
    updateBtn.classList.toggle('disabled', !isUpdateQuery);

    // Enable Query button only if exactly one database is selected
    queryBtn.disabled = !isDatabaseSelected;
    queryBtn.classList.toggle('enabled', isDatabaseSelected);
    queryBtn.classList.toggle('disabled', !isDatabaseSelected);

    // Debugging
    console.log(`Download Button Disabled: ${downloadBtn.disabled}, Update Button Disabled: ${updateBtn.disabled}, Query Button Disabled: ${queryBtn.disabled}`);
}

// Monitor changes in SQL input field
document.getElementById('sqlInput').addEventListener('input', updateButtonState);


document.getElementById('downloadBtn').onclick = async function () {
    const selectedCheckboxes = document.querySelectorAll('.db-checkbox:checked');
    const indexes = Array.from(selectedCheckboxes).map(checkbox => checkbox.value);
    const sqlQuery = document.getElementById('sqlInput').value.trim();

    // Create a progress display element
    const progressDisplay = document.getElementById('progressDisplay');
    progressDisplay.style.display = 'block'; // Show progress element
    progressDisplay.innerText = 'Downloading... %';
    
    try {
        const response = await fetch(`/api/download_unsaved_csv_files_list?query=${encodeURIComponent(sqlQuery)}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(indexes)
        });

        if (response.ok) {
            const contentLength = response.headers.get('Content-Length');
            const reader = response.body.getReader();
            const total = parseInt(contentLength, 10);
            let received = 0;

            const chunks = [];
            while (true) {
                const { done, value } = await reader.read();
                if (done) {
                    break;
                }
                chunks.push(value);
                received += value.length;

                // Update progress percentage
                const percent = ((received / total) * 100).toFixed(2);
                progressDisplay.innerText = `Downloading... ${percent}%`;
            }

            const blob = new Blob(chunks);
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = 'unsaved_csv_files_list.csv'; // Specify the filename
            document.body.appendChild(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(url);

            // Reset progress display after download completes
            progressDisplay.innerText = 'Download complete!';
        } else {
            console.error('Failed to download database');
            document.getElementById('message').innerText = 'Failed to download database.';
        }
    } catch (error) {
        console.error('Error downloading database:', error);
        document.getElementById('message').innerText = 'Error downloading database: ' + error.message;
    } finally {
        // Hide progress display
        progressDisplay.style.display = 'none';
    }
};


// Update database functionality
document.getElementById('updateBtn').onclick = async function () {
    const selectedCheckboxes = document.querySelectorAll('.db-checkbox:checked');
    const indexes = Array.from(selectedCheckboxes).map(checkbox => checkbox.value);
    const sqlQuery = document.getElementById('sqlInput').value.trim();
    
    try {
        const response = await fetch(`/api/update-db?query=${encodeURIComponent(sqlQuery)}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(indexes)
        });

        if (response.ok) {
            const result = await response.text();
            document.getElementById('message').innerText = result + ' updated or deleted successfully';
        } else {
            console.error('Failed to update database');
            document.getElementById('message').innerText = 'Failed to update database.';
        }
    } catch (error) {
        console.error('Error updating database:', error);
        document.getElementById('message').innerText = 'Error updating database: ' + error.message;
    }
};


document.getElementById('queryBtn').onclick = async function () {
    const selectedCheckboxes = document.querySelectorAll('.db-checkbox:checked');
    const progressDisplay = document.getElementById('progressDisplay');
    progressDisplay.style.display = 'block'; // Show progress element
    progressDisplay.innerText = 'Downloading... %';

    if (selectedCheckboxes.length === 1) {
        const dbIndex = selectedCheckboxes[0].value;

        try {
            const url = `/api/dump/data/${dbIndex}`;
            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({})
            });

            if (response.ok) {
                const contentLength = response.headers.get('Content-Length');
                const total = contentLength ? parseInt(contentLength, 10) : null; // Handle absence of Content-Length
                let received = 0;

                const chunks = [];
                const reader = response.body.getReader();

                while (true) {
                    const { done, value } = await reader.read();
                    if (done) {
                        break;
                    }
                    chunks.push(value);
                    received += value.length;

                    // Update progress percentage
                    if (total !== null) {
                        const percent = ((received / total) * 100).toFixed(2);
                        progressDisplay.innerText = `Downloading... ${percent}%`;
                    } else {
                        progressDisplay.innerText = `Downloading... ${received} bytes received (unknown progress)`;
                    }
                }

                const blob = new Blob(chunks);
                const downloadUrl = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = downloadUrl;
                a.download = `${dbIndex}_data_dump.sql`;
                document.body.appendChild(a);
                a.click();
                a.remove();
                window.URL.revokeObjectURL(downloadUrl);
            } else {
                console.error('Failed to download database:', response.statusText);
                document.getElementById('message').innerText = 'Failed to download database. Server responded with: ' + response.statusText;
            }
        } catch (error) {
            console.error('Error downloading database:', error);
            document.getElementById('message').innerText = 'Error downloading database: ' + error.message;
        } finally {
            progressDisplay.style.display = 'none'; // Reset progress display
        }
    } else {
        console.error('No database selected or multiple databases selected');
        document.getElementById('message').innerText = 'Please select exactly one database.';
    }
};

// Call fetchDatabases() when the page loads
window.onload = fetchDatabases;
