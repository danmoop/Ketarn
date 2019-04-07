document.addEventListener("DOMContentLoaded", ready);

function ready()
{
    console.log("%c Ketarn (beta)", "background-color: #3498db; color: white; font-size: 40px;");
}

function emptyNotesTF()
{
    document.getElementById('editNotesTextField').value = "";
}