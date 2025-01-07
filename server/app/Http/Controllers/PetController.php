<?php

namespace App\Http\Controllers;

use App\Models\Pet;
use Illuminate\Http\Request;

class PetController extends Controller
{
    public function index()
    {
        $pets = Pet::latest()->get();
        return response()->json([
            'status' => true,
            'message' => 'Pets retrieved successfully',
            'data' => $pets
        ]);
    }
}
