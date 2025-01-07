<?php

namespace App\Http\Controllers;

use App\Models\Pet;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;

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

    public function store(Request $request)
    {
        $validated = $request->validate([
            'name' => 'required|string|max:255',
            'type' => 'required|string|max:255',
            'description' => 'required|string',
            'image' => 'required|image|mimes:jpeg,png,jpg|max:2048'
        ]);

        if ($request->hasFile('image')) {
            $image = $request->file('image');
            $imageName = $image->hashName();
            $image->storeAs('images', $imageName, ['disk' => 'public']);
            $validated['image'] = asset('/storage/images/' . $imageName);
        }


        $pet = Pet::create($validated);

        return response()->json([
            'status' => true,
            'message' => 'Pet created successfully',
            'data' => $pet
        ]);
    }

    public function destroy(Pet $pet)
    {
        $imagePath = explode('/', $pet->image);
        $imageFileName = end($imagePath);

        if ($imageFileName) {
            $isImageDeleted = Storage::disk('public')->delete('images/' . $imageFileName);
        } else {
            return response()->json([
                'status' => false,
                'message' => 'Image not found'
            ], 404);
        }

        if ($isImageDeleted) {
            $pet->delete();
            return response()->json([
                'status' => true,
                'message' => 'Pet deleted successfully',
                'data' => $pet
            ]);
        }

        return response()->json([
            'status' => false,
            'message' => 'Failed to delete image'
        ], 500);
    }
}
