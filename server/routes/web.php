<?php

use App\Models\Pet;
use Illuminate\Support\Facades\Route;

Route::get('/', function () {
    $pet = Pet::find(13);
    return view('welcome', compact('pet'));
});

// Route::delete('/pets/{pet}', [App\Http\Controllers\PetController::class, 'destroy'])->name('pets.destroy');
